package ch.afdanny.technicalexercise.clientcontractapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(title = "Client Contract API", version = "v1",
                description = "API for managing clients and contracts")
)
@SpringBootApplication
public class ClientContractApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientContractApiApplication.class, args);
    }

}
