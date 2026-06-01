package de.muenchen.evasys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import java.util.List;
import org.junit.jupiter.api.Test;

class SecondaryTrainerTest {

    private ZLSOSTEVASYSRFC createData(
            String id, String anrede, String titel,
            String vorname, String nachname, String email) {
        ZLSOSTEVASYSRFC data = new ZLSOSTEVASYSRFC();
        data.setSEKTRAINERID(id);
        data.setSEKTRAINERANREDE(anrede);
        data.setSEKTRAINERTITEL(titel);
        data.setSEKTRAINERVNAME(vorname);
        data.setSEKTRAINERNNAME(nachname);
        data.setSEKTRAINERMAIL(email);
        return data;
    }

    @Test
    void testReturnsEmptyListWhenNoSecondaryTrainer() {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setSEKTRAINERID(null);

        List<SecondaryTrainer> trainers = SecondaryTrainer.fromTrainingData(trainingData);

        assertTrue(trainers.isEmpty());
    }

    @Test
    void testReturnsEmptyListWhenSecondaryTrainerIdIsBlank() {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setSEKTRAINERID("   ");

        List<SecondaryTrainer> trainers = SecondaryTrainer.fromTrainingData(trainingData);

        assertTrue(trainers.isEmpty());
    }

    @Test
    void testParsesSingleSecondaryTrainer() {
        ZLSOSTEVASYSRFC trainingData = createData(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        List<SecondaryTrainer> result = SecondaryTrainer.fromTrainingData(trainingData);

        assertEquals(1, result.size());
        SecondaryTrainer t = result.get(0);

        assertEquals("2", t.id());
        assertEquals("2", t.anrede());
        assertEquals("Prof.", t.titel());
        assertEquals("Erika", t.vorname());
        assertEquals("Musterfrau", t.nachname());
        assertEquals("erika@example.com", t.email());
    }

    @Test
    void testParsesMultipleSecondaryTrainers() {
        ZLSOSTEVASYSRFC trainingData = createData(
                "11; 22",
                "1; 2",
                "Dr.; Prof.",
                "Max; Erika",
                "Mustermann; Musterfrau",
                "max@example.com; erika@example.com");

        List<SecondaryTrainer> result = SecondaryTrainer.fromTrainingData(trainingData);

        assertEquals(2, result.size());

        SecondaryTrainer t1 = result.get(0);
        SecondaryTrainer t2 = result.get(1);

        assertEquals("11", t1.id());
        assertEquals("22", t2.id());

        assertEquals("1", t1.anrede());
        assertEquals("2", t2.anrede());

        assertEquals("Dr.", t1.titel());
        assertEquals("Prof.", t2.titel());

        assertEquals("Max", t1.vorname());
        assertEquals("Erika", t2.vorname());

        assertEquals("Mustermann", t1.nachname());
        assertEquals("Musterfrau", t2.nachname());

        assertEquals("max@example.com", t1.email());
        assertEquals("erika@example.com", t2.email());
    }

    @Test
    void testThrowsExceptionWhenListLengthsAreInconsistent() {
        ZLSOSTEVASYSRFC trainingData = createData(
                "11; 22",
                "1", // inconsistent → only 1 value
                "Dr.; Prof.",
                "Max; Erika",
                "Mustermann; Musterfrau",
                "max@example.com; erika@example.com");

        assertThrows(IllegalArgumentException.class, () -> SecondaryTrainer.fromTrainingData(trainingData));
    }

    @Test
    void testNullFieldIsHandledGracefully() {
        ZLSOSTEVASYSRFC trainingData = createData(
                "11; 22",
                null, // null field should be filled with empty values
                "Dr.; Prof.",
                "Max; Erika",
                "Mustermann; Musterfrau",
                "max@example.com; erika@example.com");

        List<SecondaryTrainer> result = SecondaryTrainer.fromTrainingData(trainingData);

        assertEquals(2, result.size());

        SecondaryTrainer t1 = result.get(0);
        SecondaryTrainer t2 = result.get(1);

        assertEquals("11", t1.id());
        assertEquals("22", t2.id());

        assertEquals("", t1.anrede());
        assertEquals("", t2.anrede());

        assertEquals("Dr.", t1.titel());
        assertEquals("Prof.", t2.titel());

        assertEquals("Max", t1.vorname());
        assertEquals("Erika", t2.vorname());

        assertEquals("Mustermann", t1.nachname());
        assertEquals("Musterfrau", t2.nachname());

        assertEquals("max@example.com", t1.email());
        assertEquals("erika@example.com", t2.email());
    }
}
