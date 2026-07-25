package com.aura.iam.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter protecting brute-force-sensitive endpoints.
 *
 * <p>El limite se aplica <b>por cuenta</b>, no por IP. Limitar por IP rompia el login en cuanto
 * varios usuarios compartian salida a internet — una demo en la misma WiFi, o el CGNAT de
 * cualquier operador movil — porque los 10 intentos por minuto eran para todos juntos y el resto
 * recibia 429 sin haber intentado nada. La IP tampoco sirve como identidad: el cliente puede
 * mandar su propio {@code X-Forwarded-For} y estrenar cubeta en cada intento.
 *
 * <p>Se conserva un limite por IP, mucho mas holgado, para frenar el volumen de un solo origen.
 * Ese si usa la <b>ultima</b> entrada de {@code X-Forwarded-For}: es la que agrega el proxy mas
 * cercano y el cliente no puede falsificarla. La primera entrada es justo la que si controla.
 *
 * <p>Usa Bucket4j con un {@link ConcurrentHashMap} en memoria. Para varias instancias hace falta
 * moverlo a Redis; con una sola instancia en Render alcanza.
 *
 * <p>Protected endpoints: {@code /api/v1/auth/login} and {@code /api/v1/auth/pin/validate}.
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    /**
     * Tope de cubetas vivas. Sin este corte el mapa crece con cada email o IP distinta que
     * aparezca, que es un vector de memoria trivial contra una instancia de 512 MB.
     */
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    /** Cuantas veces mas permisivo es el limite por IP que el limite por cuenta. */
    private static final int IP_CAPACITY_FACTOR = 12;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final long capacity;
    private final long refillTokens;
    private final long refillDurationSeconds;

    public RateLimitingFilter(long capacity, long refillTokens, long refillDurationSeconds) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDurationSeconds = refillDurationSeconds;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!isRateLimitedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // El body se buffera porque hay que mirar el email aqui y que el controller lo lea despues.
        CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);

        String account = resolveAccount(cached);
        String ip = resolveClientIp(cached);

        if (account != null && !tryConsume("account:" + path + ":" + account, capacity)) {
            reject(response, path, "account=" + account);
            return;
        }

        if (!tryConsume("ip:" + path + ":" + ip, capacity * IP_CAPACITY_FACTOR)) {
            reject(response, path, "ip=" + ip);
            return;
        }

        filterChain.doFilter(cached, response);
    }

    private boolean isRateLimitedPath(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/pin/validate");
    }

    private boolean tryConsume(String key, long bucketCapacity) {
        if (buckets.size() >= MAX_TRACKED_CLIENTS && !buckets.containsKey(key)) {
            buckets.clear();
            log.warn("Rate limit tracking map reached {} entries; cleared", MAX_TRACKED_CLIENTS);
        }
        return buckets.computeIfAbsent(key, k -> buildBucket(bucketCapacity)).tryConsume(1);
    }

    private void reject(HttpServletResponse response, String path, String who) throws IOException {
        log.warn("Rate limit exceeded for {}, path={}", who, path);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests. Please try again later.\"}");
    }

    /**
     * Saca el email del body para limitar por cuenta. Devuelve {@code null} si el body no trae
     * uno legible; en ese caso solo aplica el limite por IP y la request sigue su curso, porque
     * rechazar aqui convertiria un body malformado en un 429 en vez del 400 que corresponde.
     */
    private String resolveAccount(CachedBodyHttpServletRequest request) {
        try {
            Map<?, ?> body = objectMapper.readValue(request.getCachedBody(), Map.class);
            Object email = body.get("email");
            if (email instanceof String value && !value.isBlank()) {
                return value.trim().toLowerCase(Locale.ROOT);
            }
        } catch (Exception e) {
            log.debug("No se pudo leer el email del body para el rate limit: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Ultima entrada de {@code X-Forwarded-For}: la agrega el proxy mas cercano, asi que el
     * cliente no la controla. La primera entrada es la que si puede inventar.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] hops = xff.split(",");
            String last = hops[hops.length - 1].trim();
            if (!last.isEmpty()) {
                return last;
            }
        }
        return request.getRemoteAddr();
    }

    private Bucket buildBucket(long bucketCapacity) {
        long tokens = Math.max(1, refillTokens * bucketCapacity / Math.max(1, capacity));
        Bandwidth limit = Bandwidth.classic(
                bucketCapacity,
                Refill.greedy(tokens, Duration.ofSeconds(refillDurationSeconds))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}
