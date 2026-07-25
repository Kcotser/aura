package com.aura;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Spring Modulith verification test.
 *
 * <p>Verifies that no bounded context imports internal (non-API) classes from
 * another bounded context, enforcing the module boundary discipline.
 * This test FAILS the build if any module violates encapsulation.
 */
class ApplicationModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(AuraApplication.class);

    @Test
    void verifiesModuleStructure() {
        modules.verify();
    }
}
