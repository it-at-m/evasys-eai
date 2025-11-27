package de.muenchen.evasys.endpoint;

import com.sap.document.sap.rfc.functions.ZLSOEVASYSRFC;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.lhm.pi.evasys.afs.SITrainingASIB;
import de.muenchen.evasys.service.TrainingProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SapServiceEndpoint implements SITrainingASIB {

    private static final Logger LOGGER = LoggerFactory.getLogger(SapServiceEndpoint.class);

    private final TrainingProcessorService trainingProcessorService;

    public SapServiceEndpoint(final TrainingProcessorService trainingProcessorService) {
        this.trainingProcessorService = trainingProcessorService;
    }

    @Override
    public void siTrainingASIB(final ZLSOEVASYSRFC trainingRequest) {
        // Just for Logging
        final ZLSOSTEVASYSRFC firstItem = trainingRequest.getITEVASYSRFC().getItem().getFirst();
        LOGGER.info("Training received (first only): id={}, title={}", firstItem.getTRAININGID(), firstItem.getTRAININGTITEL());

        // Handing the data off to the TrainingProcessorService
        trainingProcessorService.processTrainingRequest(trainingRequest);
    }
}
