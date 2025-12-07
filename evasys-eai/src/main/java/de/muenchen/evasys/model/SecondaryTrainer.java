package de.muenchen.evasys.model;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record SecondaryTrainer(
        String id,
        String anrede,
        String titel,
        String vorname,
        String nachname,
        String email) {

    public static List<SecondaryTrainer> fromTrainingData(final ZLSOSTEVASYSRFC trainingData) {
        if (trainingData.getSEKTRAINERID() == null || trainingData.getSEKTRAINERID().isBlank()) {
            return List.of();
        }

        final List<String> ids = split(trainingData.getSEKTRAINERID());
        final List<String> anreden = split(trainingData.getSEKTRAINERANREDE());
        final List<String> titel = split(trainingData.getSEKTRAINERTITEL());
        final List<String> firstNames = split(trainingData.getSEKTRAINERVNAME());
        final List<String> lastNames = split(trainingData.getSEKTRAINERNNAME());
        final List<String> emails = split(trainingData.getSEKTRAINERMAIL());

        final int size = ids.size();

        if (List.of(anreden, titel, firstNames, lastNames, emails)
                .stream().anyMatch(list -> list.size() != size)) {
            throw new IllegalArgumentException("Secondary trainer lists have inconsistent length");
        }

        final List<SecondaryTrainer> trainers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            trainers.add(new SecondaryTrainer(
                    ids.get(i),
                    anreden.get(i),
                    titel.get(i),
                    firstNames.get(i),
                    lastNames.get(i),
                    emails.get(i)));
        }
        return trainers;
    }

    private static List<String> split(final String str) {
        if (str == null) {
            return List.of();
        }
        return Arrays.stream(str.split(";"))
                .map(String::trim)
                .toList();
    }
}
