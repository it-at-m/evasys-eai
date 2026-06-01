package de.muenchen.evasys.service;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.configuration.EvasysProperties;
import de.muenchen.evasys.exception.EvasysException;
import org.springframework.stereotype.Service;

@Service
public class TrainingDataNormalizationService {

    private final EvasysProperties evasysProperties;

    public TrainingDataNormalizationService(final EvasysProperties evasysProperties) {
        this.evasysProperties = evasysProperties;
    }

    public void normalize(final ZLSOSTEVASYSRFC trainingData) {
        normalizeTeilbereichId(trainingData);
        trimStrings(trainingData);
        validateMandatoryFields(trainingData);
    }

    private void normalizeTeilbereichId(final ZLSOSTEVASYSRFC trainingData) {
        if (trainingData.getTEILBEREICHID() == null || trainingData.getTEILBEREICHID().isBlank()) {
            trainingData.setTEILBEREICHID(evasysProperties.defaultTeilbereichId());
        } else {
            trainingData.setTEILBEREICHID(trainingData.getTEILBEREICHID().trim());
        }
    }

    private void trimStrings(final ZLSOSTEVASYSRFC trainingData) {
        trainingData.setTRAININGART(trim(trainingData.getTRAININGART()));
        trainingData.setTRAININGTNANZAHL(trim(trainingData.getTRAININGTNANZAHL()));
        trainingData.setTEILBEREICHID(trim(trainingData.getTEILBEREICHID()));
    }

    private void validateMandatoryFields(final ZLSOSTEVASYSRFC trainingData) {
        if (isBlank(trainingData.getTRAINER1ID())) {
            throw new EvasysException("TRAINER1ID must not be blank");
        }
        if (isBlank(trainingData.getTRAININGID())) {
            throw new EvasysException("TRAININGID must not be blank");
        }
    }

    private static String trim(final String value) {
        return value == null ? null : value.trim();
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
