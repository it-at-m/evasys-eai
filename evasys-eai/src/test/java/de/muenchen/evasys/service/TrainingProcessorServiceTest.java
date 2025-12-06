package de.muenchen.evasys.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sap.document.sap.rfc.functions.ZLSOEVASYSRFC;
import com.sap.document.sap.rfc.functions.ZLSOEVASYSRFC.ITEVASYSRFC;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvaSysException;
import de.muenchen.evasys.model.SecondaryTrainer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TrainingProcessorServiceTest {

    @Mock
    private EvaSysService evaSysMockService;

    @Mock
    private MailNotificationService mailNotificationService;

    private TrainingProcessorService trainingProcessorService;

    @BeforeEach
    public void setup() {
        trainingProcessorService = new TrainingProcessorService(evaSysMockService, mailNotificationService);
    }

    private ZLSOSTEVASYSRFC createTrainingData(String trainerId, String subunitId, String courseId) {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAINER1ID(trainerId);
        trainingData.setTEILBEREICHID(subunitId);
        trainingData.setTRAININGID(courseId);
        return trainingData;
    }

    private ZLSOEVASYSRFC createRequestWithItems(ZLSOSTEVASYSRFC... trainingData) {
        ZLSOEVASYSRFC trainingRequest = new ZLSOEVASYSRFC();
        ITEVASYSRFC rfcItemContainer = new ZLSOEVASYSRFC.ITEVASYSRFC();
        trainingRequest.setITEVASYSRFC(rfcItemContainer);
        rfcItemContainer.getItem().addAll(List.of(trainingData));
        return trainingRequest;
    }

    @Test
    public void testThatTrainerIsUpdatedIfTrainerExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evaSysMockService.trainerExists(anyInt(), anyInt())).thenReturn(true);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evaSysMockService, times(1)).updateTrainer(trainingData);
        verify(evaSysMockService, never()).insertTrainer(trainingData);
    }

    @Test
    public void testThatTrainerIsInsertedIfTrainerDoesNotExist() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evaSysMockService.trainerExists(anyInt(), anyInt())).thenReturn(false);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evaSysMockService, never()).updateTrainer(trainingData);
        verify(evaSysMockService, times(1)).insertTrainer(trainingData);
    }

    @Test
    public void testThatSecondaryTrainerIsUpdatedIfExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        trainingData.setSEKTRAINERID("2");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evaSysMockService.trainerExists(1, 1)).thenReturn(true);

        SecondaryTrainer secondaryTrainer = new SecondaryTrainer(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        when(evaSysMockService.trainerExists(2, 1)).thenReturn(true);

        when(evaSysMockService.extractSecondaryTrainers(trainingData))
                .thenReturn(List.of(secondaryTrainer));

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evaSysMockService, times(1)).updateSecondaryTrainer(trainingData, secondaryTrainer);
        verify(evaSysMockService, never()).insertSecondaryTrainer(trainingData, secondaryTrainer);
    }

    @Test
    public void testThatSecondaryTrainerIsInsertedIfNotExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        trainingData.setSEKTRAINERID("2");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evaSysMockService.trainerExists(1, 1)).thenReturn(true);

        SecondaryTrainer secondaryTrainer = new SecondaryTrainer(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        when(evaSysMockService.trainerExists(2, 1)).thenReturn(false);

        when(evaSysMockService.extractSecondaryTrainers(trainingData))
                .thenReturn(List.of(secondaryTrainer));

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evaSysMockService, never()).updateSecondaryTrainer(trainingData, secondaryTrainer);
        verify(evaSysMockService, times(1)).insertSecondaryTrainer(trainingData, secondaryTrainer);
    }

    @Test
    public void testThatNoSecondaryTrainerMethodsCalledIfNoSecondaryTrainer() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        trainingData.setSEKTRAINERID(null);
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evaSysMockService.trainerExists(1, 1)).thenReturn(true);

        when(evaSysMockService.extractSecondaryTrainers(trainingData)).thenReturn(List.of());

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evaSysMockService, never()).updateSecondaryTrainer(any(), any());
        verify(evaSysMockService, never()).insertSecondaryTrainer(any(), any());
    }

    @Test
    public void testThatCourseIsUpdatedIfCourseExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evaSysMockService.courseExists(anyInt())).thenReturn(true);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evaSysMockService, times(1)).updateCourse(trainingData);
        verify(evaSysMockService, never()).insertCourse(trainingData);
    }

    @Test
    public void testThatCourseIsInsertedIfCourseDoesNotExist() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evaSysMockService.courseExists(anyInt())).thenReturn(false);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evaSysMockService, never()).updateCourse(trainingData);
        verify(evaSysMockService, times(1)).insertCourse(trainingData);
    }

    @Test
    public void testThatMultipleTrainingsCanBeProcessed() {
        ZLSOSTEVASYSRFC trainingData1 = createTrainingData("1", "1", "1");
        ZLSOSTEVASYSRFC trainingData2 = createTrainingData("2", "2", "2");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData1, trainingData2);

        when(evaSysMockService.trainerExists(anyInt(), anyInt())).thenReturn(false);
        when(evaSysMockService.courseExists(anyInt())).thenReturn(false);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evaSysMockService, never()).updateTrainer(any());
        verify(evaSysMockService, times(2)).insertTrainer(any());
        verify(evaSysMockService, never()).updateCourse(any());
        verify(evaSysMockService, times(2)).insertCourse(any());
    }

    @Test
    public void testThatNotifyErrorIsCalledWhenTrainerProcessingFails() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evaSysMockService.trainerExists(anyInt(), anyInt())).thenThrow(new EvaSysException("Trainer error"));

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(mailNotificationService, times(1)).notifyError(
                eq("Trainer processing failed"),
                eq("Trainer error"),
                any(EvaSysException.class),
                eq(trainingData));
    }

    @Test
    public void testNotifyErrorIsCalledWhenCourseProcessingFails() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evaSysMockService.trainerExists(anyInt(), anyInt())).thenReturn(true);
        when(evaSysMockService.courseExists(anyInt())).thenThrow(new EvaSysException("Course error"));

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(mailNotificationService, times(1)).notifyError(
                eq("Course processing failed"),
                eq("Course error"),
                any(EvaSysException.class),
                eq(trainingData));
    }
}
