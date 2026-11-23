package cl.duocuc.induztek.roles;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class RolesServiceApplicationTest {

    @Test
    void iniciaAplicacionSpringBoot() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            new RolesServiceApplication();
            RolesServiceApplication.main(new String[]{"--test"});

            springApplication.verify(() -> SpringApplication.run(
                    RolesServiceApplication.class, new String[]{"--test"}));
        }
    }
}
