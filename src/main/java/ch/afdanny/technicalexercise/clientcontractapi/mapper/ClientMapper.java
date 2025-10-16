package ch.afdanny.technicalexercise.clientcontractapi.mapper;

import ch.afdanny.technicalexercise.clientcontractapi.dto.response.CompanyClientResponse;
import ch.afdanny.technicalexercise.clientcontractapi.dto.response.PersonClientResponse;
import ch.afdanny.technicalexercise.clientcontractapi.model.CompanyClient;
import ch.afdanny.technicalexercise.clientcontractapi.model.PersonClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "type", constant = "person")
    PersonClientResponse toPersonResponse(PersonClient person);

    @Mapping(target = "type", constant = "company")
    CompanyClientResponse toCompanyResponse(CompanyClient company);
}