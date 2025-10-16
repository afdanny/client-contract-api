package ch.afdanny.technicalexercise.clientcontractapi.dto.response;

import com.fasterxml.jackson.annotation.*;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PersonClientResponse.class, name = "PERSON"),
        @JsonSubTypes.Type(value = CompanyClientResponse.class, name = "COMPANY")
})
public abstract class ClientResponse {
    public UUID id;
    public String name;
    public String email;
    public String phone;
    public String type; // "PERSON" | "COMPANY"
}