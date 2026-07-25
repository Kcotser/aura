package com.aura.directory.infrastructure.config;

import com.aura.directory.domain.model.GeoLocation;
import com.aura.directory.domain.model.Institution;
import com.aura.directory.domain.model.InstitutionType;
import com.aura.directory.domain.repository.InstitutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeder that runs on application startup to prepopulate institutional directory data if empty.
 */
@Component
public class DirectoryDatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DirectoryDatabaseSeeder.class);

    private final InstitutionRepository repository;

    public DirectoryDatabaseSeeder(InstitutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            log.info("Seeding institutional directory catalog for Peru...");

            repository.save(Institution.create(
                    "linea-100",
                    "Línea 100",
                    InstitutionType.HELPLINE,
                    "100",
                    "Servicio telefónico gratuito de orientación y soporte emocional para víctimas de violencia familiar o sexual.",
                    null
            ));

            repository.save(Institution.create(
                    "pnp",
                    "Policía Nacional del Perú (PNP)",
                    InstitutionType.POLICE,
                    "105",
                    "Servicio de emergencias policiales a nivel nacional.",
                    null
            ));

            repository.save(Institution.create(
                    "comisaria-mujer",
                    "Comisaría de la Mujer y Poblaciones Vulnerables",
                    InstitutionType.POLICE,
                    "01-4278153",
                    "Jirón Cotabambas 154, Cercado de Lima",
                    new GeoLocation(-12.054361, -77.032333)
            ));

            repository.save(Institution.create(
                    "cem",
                    "Centro Emergencia Mujer (CEM Lima)",
                    InstitutionType.SHELTER,
                    "01-4197060",
                    "Avenida Alfonso Ugarte 806, Cercado de Lima",
                    new GeoLocation(-12.046111, -77.042778)
            ));

            repository.save(Institution.create(
                    "defensoria-pueblo",
                    "Defensoría del Pueblo Perú",
                    InstitutionType.NGO,
                    "0800-15-170",
                    "Jirón Ucayali 388, Cercado de Lima",
                    new GeoLocation(-12.048056, -77.029444)
            ));

            log.info("Institutional directory seeding completed successfully. Seeded {} records.", repository.count());
        } else {
            log.info("Institutional directory database already has records. Seeding skipped.");
        }
    }
}
