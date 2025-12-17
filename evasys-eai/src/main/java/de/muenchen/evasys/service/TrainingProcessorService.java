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

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingProcessorService.class);

    public TrainingProcessorService(final EvasysService evasysService, final MailNotificationService mailNotificationService) {
        this.evasysService = evasysService;
        this.mailNotificationService = mailNotificationService;
    }

    public void processTrainingRequest(final ZLSOEVASYSRFC trainingRequest) {
        LOGGER.info("Processing training requests...");
        for (final ZLSOSTEVASYSRFC trainingData : trainingRequest.getITEVASYSRFC().getItem()) {
            try {
                processTrainer(trainingData);
            } catch (EvasysException e) {
                LOGGER.error("Trainer processing failed: {}", e.getMessage());
                mailNotificationService.notifyError("Trainer processing failed", e.getMessage(), e, trainingData);
            }

            try {
                processCourse(trainingData);
            } catch (EvasysException e) {
                LOGGER.error("Course processing failed: {}", e.getMessage());
                mailNotificationService.notifyError("Course processing failed", e.getMessage(), e, trainingData);
            }
        }
        LOGGER.info("All training requests processed");
    }

    private void processTrainer(final ZLSOSTEVASYSRFC trainingData) {
        normalizeTeilbereichId(trainingData);
        final int trainerId = Integer.parseInt(trainingData.getTRAINER1ID());
        final int subunitId = Integer.parseInt(trainingData.getTEILBEREICHID());

        if (evasysService.trainerExists(trainerId, subunitId)) {
            evasysService.updateTrainer(trainingData);
        } else {
            evasysService.insertTrainer(trainingData);
        }

        final List<SecondaryTrainer> trainers = evasysService.extractSecondaryTrainers(trainingData);

        for (final SecondaryTrainer trainer : trainers) {
            final int secondaryTrainerId = Integer.parseInt(trainer.id());

            if (evasysService.trainerExists(secondaryTrainerId, subunitId)) {
                evasysService.updateSecondaryTrainer(trainingData, trainer);
            } else {
                evasysService.insertSecondaryTrainer(trainingData, trainer);
            }
        }
    }

    private void processCourse(final ZLSOSTEVASYSRFC trainingData) {
        normalizeTeilbereichId(trainingData);
        final int courseId = Integer.parseInt(trainingData.getTRAININGID());

        if (evasysService.courseExists(courseId)) {
            evasysService.updateCourse(trainingData);
        } else {
            evasysService.insertCourse(trainingData);
        }
    }

    private static void normalizeTeilbereichId(final ZLSOSTEVASYSRFC trainingData) {
        if (trainingData.getTEILBEREICHID() == null || trainingData.getTEILBEREICHID().isBlank()) {
            trainingData.setTEILBEREICHID("5");
        }
    }
}
