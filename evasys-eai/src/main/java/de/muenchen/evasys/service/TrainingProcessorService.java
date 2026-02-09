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

        insertTrainerOrUpdateIfExists(trainerId, subunitId, trainingData);

        final List<SecondaryTrainer> trainers = evasysService.extractSecondaryTrainers(trainingData);

        for (final SecondaryTrainer trainer : trainers) {
            final String secondaryTrainerId = trainer.id();
            insertSecondaryTrainerOrUpdateIfExists(secondaryTrainerId, subunitId, trainingData, trainer);
        }
    }

    private void insertTrainerOrUpdateIfExists(
            final String trainerId,
            final int subunitId,
            final ZLSOSTEVASYSRFC trainingData) {
        try {
            if (evasysService.trainerExists(trainerId, subunitId)) {
                evasysService.updateTrainer(trainingData);
            } else {
                evasysService.insertTrainer(trainingData);
            }
        } catch (EvasysException e) {
            if (evasysService.trainerExists(trainerId, subunitId)) {
                LOGGER.info("Trainer {} already created by concurrent request, updating instead", trainerId);
                evasysService.updateTrainer(trainingData);
            } else {
                throw e;
            }
        }
    }

    private void insertSecondaryTrainerOrUpdateIfExists(
            final String secondaryTrainerId,
            final int subunitId,
            final ZLSOSTEVASYSRFC trainingData,
            final SecondaryTrainer trainer) {
        try {
            if (evasysService.trainerExists(secondaryTrainerId, subunitId)) {
                evasysService.updateSecondaryTrainer(trainer);
            } else {
                evasysService.insertSecondaryTrainer(trainingData, trainer);
            }
        } catch (EvasysException e) {
            if (evasysService.trainerExists(secondaryTrainerId, subunitId)) {
                LOGGER.info("Secondary trainer {} already created by concurrent request, updating instead", secondaryTrainerId);
                evasysService.updateSecondaryTrainer(trainer);
            } else {
                throw e;
            }
        }
    }

    private void processCourse(final ZLSOSTEVASYSRFC trainingData) {
        final int courseId = Integer.parseInt(trainingData.getTRAININGID());

        try {
            if (evasysService.courseExists(courseId)) {
                evasysService.updateCourse(trainingData);
            } else {
                evasysService.insertCourse(trainingData);
            }
        } catch (EvasysException e) {
            if (evasysService.courseExists(courseId)) {
                LOGGER.info("Course {} already created by concurrent request, updating instead", courseId);
                evasysService.updateCourse(trainingData);
            } else {
                throw e;
            }
        }
    }
}
