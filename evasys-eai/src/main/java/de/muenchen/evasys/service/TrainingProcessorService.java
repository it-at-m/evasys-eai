package de.muenchen.evasys.service;

import com.sap.document.sap.rfc.functions.ZLSOEVASYSRFC;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.configuration.EvaSysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TrainingProcessorService {

    private final EvaSysService evaSysService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingProcessorService.class);

    public TrainingProcessorService(final EvaSysService evaSysService) {
        this.evaSysService = evaSysService;
    }

    public void processTrainingRequest(final ZLSOEVASYSRFC trainingRequest) {
        LOGGER.info("Processing training requests...");
        for (final ZLSOSTEVASYSRFC trainingData : trainingRequest.getITEVASYSRFC().getItem()) {
            try {
                processTrainer(trainingData);
            } catch (EvaSysException e) {
                LOGGER.error("Trainer processing failed: {}", e.getMessage());
            }

            try {
                processCourse(trainingData);
            } catch (EvaSysException e) {
                LOGGER.error("Course processing failed: {}", e.getMessage());
            }
        }
        LOGGER.info("All training requests processed");
    }

    private void processTrainer(final ZLSOSTEVASYSRFC trainingData) {
        final int trainerId = Integer.parseInt(trainingData.getTRAINER1ID());
        final int subunitId = Integer.parseInt(trainingData.getTEILBEREICHID());

        if (evaSysService.trainerExists(trainerId, subunitId)) {
            evaSysService.updateTrainer(trainingData);
        } else {
            evaSysService.insertTrainer(trainingData);
        }

        if (evaSysService.hasSecondaryTrainer(trainingData)) {
            final int secondaryTrainerId = Integer.parseInt(trainingData.getSEKTRAINERID());

            if (evaSysService.trainerExists(secondaryTrainerId, subunitId)) {
                evaSysService.updateSecondaryTrainer(trainingData);
            } else {
                evaSysService.insertSecondaryTrainer(trainingData);
            }
        }
    }

    private void processCourse(final ZLSOSTEVASYSRFC trainingData) {
        final int courseId = Integer.parseInt(trainingData.getTRAININGID());

        if (evaSysService.courseExists(courseId)) {
            evaSysService.updateCourse(trainingData);
        } else {
            evaSysService.insertCourse(trainingData);
        }
    }
}
