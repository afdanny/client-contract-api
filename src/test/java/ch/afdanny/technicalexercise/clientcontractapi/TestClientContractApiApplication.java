package ch.afdanny.technicalexercise.clientcontractapi;

import org.springframework.boot.SpringApplication;

public class TestClientContractApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(ClientContractApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
