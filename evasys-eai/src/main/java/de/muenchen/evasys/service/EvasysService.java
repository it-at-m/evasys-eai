package de.muenchen.evasys.service;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.client.EvasysClient;
import de.muenchen.evasys.model.SecondaryTrainer;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EvasysService {

    private final EvasysClient client;

    public EvasysService(final EvasysClient client) {
        this.client = client;
    }

    public boolean trainerExists(final int trainerId, final int subunitId) {
        return client.isTrainerExisting(trainerId, subunitId);
    }

    public void updateTrainer(final ZLSOSTEVASYSRFC trainingData) {
        client.updateTrainer(trainingData);
    }

    public void insertTrainer(final ZLSOSTEVASYSRFC trainingData) {
        client.insertTrainer(trainingData);
    }

    public List<SecondaryTrainer> extractSecondaryTrainers(final ZLSOSTEVASYSRFC trainingData) {
        return SecondaryTrainer.fromTrainingData(trainingData);
    }

    public void updateSecondaryTrainer(final ZLSOSTEVASYSRFC trainingData, final SecondaryTrainer secondaryTrainer) {
        client.updateSecondaryTrainer(trainingData, secondaryTrainer);
    }

    public void insertSecondaryTrainer(final ZLSOSTEVASYSRFC trainingData, final SecondaryTrainer secondaryTrainer) {
        client.insertSecondaryTrainer(trainingData, secondaryTrainer);
    }

    public boolean courseExists(final int courseId) {
        return client.isCourseExisting(courseId);
    }

    public void updateCourse(final ZLSOSTEVASYSRFC trainingData) {
        client.updateCourse(trainingData);
    }

    public void insertCourse(final ZLSOSTEVASYSRFC trainingData) {
        client.insertCourse(trainingData);
    }
}
