package cl.duocuc.induztek.users;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class UsersServiceApplicationTest {

    @Test
    void iniciaAplicacionSpringBoot() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            new UsersServiceApplication();
            UsersServiceApplication.main(new String[]{"--test"});

            springApplication.verify(() -> SpringApplication.run(
                    UsersServiceApplication.class, new String[]{"--test"}));
        }
    }
}
