package de.muenchen.evasys.mapper;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.model.SecondaryTrainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import wsdl.soapserver_v100.Course;
import wsdl.soapserver_v100.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SapEvaSysMapper {

    @Mapping(target = "MNType", constant = "1")
    @Mapping(source = "TRAINER1ID", target = "MSExternalId")
    @Mapping(source = "TRAINER1TITEL", target = "MSTitle")
    @Mapping(source = "TRAINER1VNAME", target = "MSFirstName")
    @Mapping(source = "TRAINER1NNAME", target = "MSSurName")
    @Mapping(source = "TRAINER1MAIL", target = "MSEmail")
    @Mapping(source = "TEILBEREICHID", target = "MNFbid")
    @Mapping(source = "TRAINERGESCHL", target = "MNAddressId")
    User mapToTrainer(ZLSOSTEVASYSRFC trainingData);

    @Mapping(target = "MNType", constant = "1")
    @Mapping(source = "secondaryTrainer.id", target = "MSExternalId")
    @Mapping(source = "secondaryTrainer.titel", target = "MSTitle")
    @Mapping(source = "secondaryTrainer.vorname", target = "MSFirstName")
    @Mapping(source = "secondaryTrainer.nachname", target = "MSSurName")
    @Mapping(source = "secondaryTrainer.email", target = "MSEmail")
    @Mapping(source = "secondaryTrainer.anrede", target = "MNAddressId")
    @Mapping(source = "trainingData.TEILBEREICHID", target = "MNFbid")
    User mapToSecondaryTrainer(SecondaryTrainer secondaryTrainer, ZLSOSTEVASYSRFC trainingData);

    @Mapping(source = "TRAININGSTYPKUERZEL", target = "MSProgramOfStudy")
    @Mapping(source = "TRAININGTITEL", target = "MSCourseTitle")
    @Mapping(source = "TRAININGRAUM", target = "MSRoom")
    @Mapping(source = "TRAININGART", target = "MNCourseType")
    @Mapping(source = "TRAININGID", target = "MSPubCourseId")
    @Mapping(source = "TRAININGTNANZAHL", target = "MNCountStud")
    @Mapping(source = "TEILBEREICHID", target = "MNFbid")
    Course mapToCourse(ZLSOSTEVASYSRFC trainingData);
}
