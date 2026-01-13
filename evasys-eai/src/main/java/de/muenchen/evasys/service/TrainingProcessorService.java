package de.muenchen.evasys.service;

import com.sap.document.sap.rfc.functions.ZLSOEVASYSRFC;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvasysException;
import de.muenchen.evasys.model.SecondaryTrainer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TrainingProcessorService {

    private final EvasysService evasysService;

    private final MailNotificationService mailNotificationService;

    private final TrainingDataNormalizationService normalizationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingProcessorService.class);

    public TrainingProcessorService(final EvasysService evasysService, final MailNotificationService mailNotificationService,
            final TrainingDataNormalizationService normalizationService) {
        this.evasysService = evasysService;
        this.mailNotificationService = mailNotificationService;
        this.normalizationService = normalizationService;
    }

    public void processTrainingRequest(final ZLSOEVASYSRFC trainingRequest) {
        LOGGER.info("Processing training requests...");
        for (final ZLSOSTEVASYSRFC trainingData : trainingRequest.getITEVASYSRFC().getItem()) {
            try {
                normalizationService.normalize(trainingData);
                processTrainer(trainingData);
                processCourse(trainingData);
            } catch (EvasysException e) {
                LOGGER.error("Processing failed: {}", e.getMessage());
                mailNotificationService.notifyError(
                        "Processing failed",
                        e.getMessage(),
                        e,
                        trainingData);
            }
        }
        LOGGER.info("All training requests processed");
    }

    private void processTrainer(final ZLSOSTEVASYSRFC trainingData) {
        final String trainerId = trainingData.getTRAINER1ID();
        final int subunitId = Integer.parseInt(trainingData.getTEILBEREICHID());

        if (evasysService.trainerExists(trainerId, subunitId)) {
            evasysService.updateTrainer(trainingData);
        } else {
            evasysService.insertTrainer(trainingData);
        }

        final List<SecondaryTrainer> trainers = evasysService.extractSecondaryTrainers(trainingData);

        for (final SecondaryTrainer trainer : trainers) {
            final String secondaryTrainerId = trainer.id();

            if (evasysService.trainerExists(secondaryTrainerId, subunitId)) {
                evasysService.updateSecondaryTrainer(trainer);
            } else {
                evasysService.insertSecondaryTrainer(trainingData, trainer);
            }
        }
    }

    private void processCourse(final ZLSOSTEVASYSRFC trainingData) {
        final int courseId = Integer.parseInt(trainingData.getTRAININGID());

        if (evasysService.courseExists(courseId)) {
            evasysService.updateCourse(trainingData);
        } else {
            evasysService.insertCourse(trainingData);
        }
    }
}
