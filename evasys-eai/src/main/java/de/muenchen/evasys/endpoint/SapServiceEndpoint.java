package de.muenchen.evasys.endpoint;

import com.sap.document.sap.rfc.functions.ZLSOEVASYSRFC;
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
        try {
            trainingProcessorService.processTrainingRequest(trainingRequest);
        } catch (Exception e) {
            LOGGER.error("Error processing SAP message", e);
        }
    }
}
