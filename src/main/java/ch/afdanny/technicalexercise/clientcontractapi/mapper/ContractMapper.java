package ch.afdanny.technicalexercise.clientcontractapi.mapper;

import ch.afdanny.technicalexercise.clientcontractapi.dto.response.ContractResponse;
import ch.afdanny.technicalexercise.clientcontractapi.model.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(target = "clientId", source = "client.id")
    ContractResponse toResponse(Contract contract);
}