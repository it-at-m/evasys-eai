package de.muenchen.evasys.service;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.client.EvasysCourseClient;
import de.muenchen.evasys.client.EvasysUserClient;
import de.muenchen.evasys.model.SecondaryTrainer;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EvasysService {

    private final EvasysUserClient userClient;
    private final EvasysCourseClient courseClient;

    public EvasysService(
            final EvasysUserClient userClient,
            final EvasysCourseClient courseClient) {
        this.userClient = userClient;
        this.courseClient = courseClient;
    }

    public boolean trainerExists(final String trainerId, final int subunitId) {
        return userClient.isTrainerExisting(trainerId, subunitId);
    }

    public void updateTrainer(final ZLSOSTEVASYSRFC trainingData) {
        userClient.updateTrainer(trainingData);
    }

    public void insertTrainer(final ZLSOSTEVASYSRFC trainingData) {
        userClient.insertTrainer(trainingData);
    }

    public List<SecondaryTrainer> extractSecondaryTrainers(final ZLSOSTEVASYSRFC trainingData) {
        return SecondaryTrainer.fromTrainingData(trainingData);
    }

    public void updateSecondaryTrainer(final SecondaryTrainer secondaryTrainer) {
        userClient.updateSecondaryTrainer(secondaryTrainer);
    }

    public void insertSecondaryTrainer(final ZLSOSTEVASYSRFC trainingData, final SecondaryTrainer secondaryTrainer) {
        userClient.insertSecondaryTrainer(trainingData, secondaryTrainer);
    }

    public boolean courseExists(final int courseId) {
        return courseClient.isCourseExisting(courseId);
    }

    public void updateCourse(final ZLSOSTEVASYSRFC trainingData) {
        courseClient.updateCourse(trainingData);
    }

    public void insertCourse(final ZLSOSTEVASYSRFC trainingData) {
        courseClient.insertCourse(trainingData);
    }
}
