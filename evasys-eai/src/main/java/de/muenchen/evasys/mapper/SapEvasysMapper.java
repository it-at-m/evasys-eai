package de.muenchen.evasys.mapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.model.SecondaryTrainer;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import wsdl.soapserver_v100.Course;
import wsdl.soapserver_v100.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SapEvasysMapper {

    @Mapping(target = "MNType", constant = "1")
    @Mapping(target = "MBActiveUser", constant = "true")
    @Mapping(source = "TRAINER1ID", target = "MSExternalId")
    @Mapping(source = "TRAINER1ANREDE", target = "MNAddressId")
    @Mapping(source = "TRAINER1TITEL", target = "MSTitle")
    @Mapping(source = "TRAINER1VNAME", target = "MSFirstName")
    @Mapping(source = "TRAINER1NNAME", target = "MSSurName")
    @Mapping(source = "TRAINER1MAIL", target = "MSEmail")
    @Mapping(source = "TEILBEREICHID", target = "MNFbid")
    User mapToTrainer(ZLSOSTEVASYSRFC trainingData);

    @Mapping(target = "MNType", constant = "1")
    @Mapping(target = "MBActiveUser", constant = "true")
    @Mapping(source = "secondaryTrainer.id", target = "MSExternalId")
    @Mapping(source = "secondaryTrainer.titel", target = "MSTitle")
    @Mapping(source = "secondaryTrainer.vorname", target = "MSFirstName")
    @Mapping(source = "secondaryTrainer.nachname", target = "MSSurName")
    @Mapping(source = "secondaryTrainer.email", target = "MSEmail")
    @Mapping(source = "secondaryTrainer.anrede", target = "MNAddressId")
    @Mapping(source = "trainingData.TEILBEREICHID", target = "MNFbid")
    User mapToSecondaryTrainer(SecondaryTrainer secondaryTrainer, ZLSOSTEVASYSRFC trainingData);

    @Mapping(target = "hasAnonymousParticipants", constant = "true")
    @Mapping(source = "TRAININGSTYPKUERZEL", target = "MSProgramOfStudy")
    @Mapping(source = "TRAININGTITEL", target = "MSCourseTitle")
    @Mapping(source = "TRAININGRAUM", target = "MSRoom")
    @Mapping(source = "TRAININGART", target = "MNCourseType")
    @Mapping(source = "TRAININGID", target = "MSPubCourseId")
    @Mapping(source = "TRAININGTNANZAHL", target = "MNCountStud")
    @Mapping(source = "TEILBEREICHID", target = "MNFbid")
    Course mapToCourse(ZLSOSTEVASYSRFC trainingData);

    @AfterMapping
    default void buildCustomFieldsJson(final ZLSOSTEVASYSRFC source, @MappingTarget final Course target) {
        final ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("1", source.getTRAINERGESCHL());
        json.put("2", source.getTRAINEROBJTYP());
        json.put("3", source.getFIRMA());
        json.put("4", ""); // TODO: intentionally empty; clarification pending as to whether still needed
        json.put("5", ""); // TODO: intentionally empty; clarification pending as to whether still needed
        json.put("6", source.getTEILBEREICHID());
        json.put("7", source.getTRAININGBEGINN());
        json.put("8", source.getTRAININGENDE());
        json.put("9", String.join(" ", source.getVAVNAME(), source.getVANNAME()));
        json.put("10", String.join(" ", source.getSBVNAME(), source.getSBNNAME()));
        json.put("11", source.getTRAININGDAUERTAGE());
        json.put("12", source.getTRAININGDAUERSTD());
        target.setMSCustomFieldsJSON(json.toString());
    }
}
