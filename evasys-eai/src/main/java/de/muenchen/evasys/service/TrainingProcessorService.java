package de.muenchen.evasys.service;

import com.sap.document.sap.rfc.functions.ZLSOEVASYSRFC;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvaSysException;
import de.muenchen.evasys.model.SecondaryTrainer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TrainingProcessorService {

    private final EvaSysService evaSysService;

    private final MailNotificationService mailNotificationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingProcessorService.class);

    public TrainingProcessorService(final EvaSysService evaSysService, final MailNotificationService mailNotificationService) {
        this.evaSysService = evaSysService;
        this.mailNotificationService = mailNotificationService;
    }

    public void processTrainingRequest(final ZLSOEVASYSRFC trainingRequest) {
        LOGGER.info("Processing training requests...");
        for (final ZLSOSTEVASYSRFC trainingData : trainingRequest.getITEVASYSRFC().getItem()) {
            try {
                processTrainer(trainingData);
            } catch (EvaSysException e) {
                LOGGER.error("Trainer processing failed: {}", e.getMessage());
                mailNotificationService.notifyError("Trainer processing failed", e.getMessage(), e, trainingData);
            }

            try {
                processCourse(trainingData);
            } catch (EvaSysException e) {
                LOGGER.error("Course processing failed: {}", e.getMessage());
                mailNotificationService.notifyError("Course processing failed", e.getMessage(), e, trainingData);
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

        final List<SecondaryTrainer> trainers = evaSysService.extractSecondaryTrainers(trainingData);

        for (final SecondaryTrainer trainer : trainers) {
            final int secondaryTrainerId = Integer.parseInt(trainer.id());

            if (evaSysService.trainerExists(secondaryTrainerId, subunitId)) {
                evaSysService.updateSecondaryTrainer(trainingData, trainer);
            } else {
                evaSysService.insertSecondaryTrainer(trainingData, trainer);
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
