package ch.afdanny.technicalexercise.clientcontractapi.mapper;

import ch.afdanny.technicalexercise.clientcontractapi.dto.response.CompanyClientResponse;
import ch.afdanny.technicalexercise.clientcontractapi.dto.response.PersonClientResponse;
import ch.afdanny.technicalexercise.clientcontractapi.model.CompanyClient;
import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "birthdate", source = "birthdate")
    @Mapping(target = "type", expression = "java(person.getType().name().toLowerCase(java.util.Locale.ROOT))")
    PersonClientResponse toPersonResponse(PersonClient person);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "companyIdentifier", source = "companyIdentifier")
    @Mapping(target = "type", expression = "java(company.getType().name().toLowerCase(java.util.Locale.ROOT))")
    CompanyClientResponse toCompanyResponse(CompanyClient company);
}