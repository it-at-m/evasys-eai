package de.muenchen.evasys.model;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record SecondaryTrainer(
        String id,
        String anrede,
        String titel,
        String vorname,
        String nachname,
        String email) {

    public static List<SecondaryTrainer> fromTrainingData(
            final ZLSOSTEVASYSRFC trainingData) {

        if (trainingData == null
                || trainingData.getSEKTRAINERID() == null
                || trainingData.getSEKTRAINERID().isBlank()) {
            return List.of();
        }

        final List<String> ids = splitIds(trainingData.getSEKTRAINERID());
        final int size = ids.size();

        final List<String> addresses = splitOrFill(trainingData.getSEKTRAINERANREDE(), size);
        final List<String> titles = splitOrFill(trainingData.getSEKTRAINERTITEL(), size);
        final List<String> firstNames = splitOrFill(trainingData.getSEKTRAINERVNAME(), size);
        final List<String> lastNames = splitOrFill(trainingData.getSEKTRAINERNNAME(), size);
        final List<String> emails = splitOrFill(trainingData.getSEKTRAINERMAIL(), size);

        final List<SecondaryTrainer> trainers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            trainers.add(new SecondaryTrainer(
                    ids.get(i),
                    addresses.get(i),
                    titles.get(i),
                    firstNames.get(i),
                    lastNames.get(i),
                    emails.get(i)));
        }

        return trainers;
    }

    private static List<String> splitIds(final String str) {
        return Arrays.stream(str.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static List<String> splitOrFill(final String str, final int expectedSize) {
        if (str == null || str.isBlank()) {
            return new ArrayList<>(Collections.nCopies(expectedSize, ""));
        }

        final List<String> values = Arrays.stream(str.split(";"))
                .map(String::trim)
                .toList();

        if (values.size() != expectedSize) {
            throw new IllegalArgumentException(
                    "Secondary trainer lists have inconsistent length");
        }

        return values;
    }
}
