package de.muenchen.evasys.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sap.document.sap.rfc.functions.ZLSOEVASYSRFC;
import com.sap.document.sap.rfc.functions.ZLSOEVASYSRFC.ITEVASYSRFC;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvasysException;
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
    private EvasysService evasysMockService;

    @Mock
    private MailNotificationService mailNotificationService;

    @Mock
    private TrainingDataNormalizationService normalizationService;

    private TrainingProcessorService trainingProcessorService;

    @BeforeEach
    public void setup() {
        trainingProcessorService = new TrainingProcessorService(evasysMockService, mailNotificationService, normalizationService);
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

        when(evasysMockService.trainerExists(anyString(), anyInt())).thenReturn(true);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, times(1)).updateTrainer(trainingData);
        verify(evasysMockService, never()).insertTrainer(trainingData);
    }

    @Test
    public void testThatTrainerIsInsertedIfTrainerDoesNotExist() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists(anyString(), anyInt())).thenReturn(false);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, never()).updateTrainer(trainingData);
        verify(evasysMockService, times(1)).insertTrainer(trainingData);
    }

    @Test
    public void testThatSecondaryTrainerIsUpdatedIfExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        trainingData.setSEKTRAINERID("2");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists("1", 1)).thenReturn(true);

        SecondaryTrainer secondaryTrainer = new SecondaryTrainer(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        when(evasysMockService.trainerExists("2", 1)).thenReturn(true);

        when(evasysMockService.extractSecondaryTrainers(trainingData))
                .thenReturn(List.of(secondaryTrainer));

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, times(1)).updateSecondaryTrainer(secondaryTrainer);
        verify(evasysMockService, never()).insertSecondaryTrainer(trainingData, secondaryTrainer);
    }

    @Test
    public void testThatSecondaryTrainerIsInsertedIfNotExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        trainingData.setSEKTRAINERID("2");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists("1", 1)).thenReturn(true);

        SecondaryTrainer secondaryTrainer = new SecondaryTrainer(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        when(evasysMockService.trainerExists("2", 1)).thenReturn(false);

        when(evasysMockService.extractSecondaryTrainers(trainingData))
                .thenReturn(List.of(secondaryTrainer));

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, never()).updateSecondaryTrainer(secondaryTrainer);
        verify(evasysMockService, times(1)).insertSecondaryTrainer(trainingData, secondaryTrainer);
    }

    @Test
    public void testThatNoSecondaryTrainerMethodsCalledIfNoSecondaryTrainer() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        trainingData.setSEKTRAINERID(null);
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists("1", 1)).thenReturn(true);

        when(evasysMockService.extractSecondaryTrainers(trainingData)).thenReturn(List.of());

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, never()).updateSecondaryTrainer(any());
        verify(evasysMockService, never()).insertSecondaryTrainer(any(), any());
    }

    @Test
    public void testThatCourseIsUpdatedIfCourseExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.courseExists(anyInt())).thenReturn(true);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, times(1)).updateCourse(trainingData);
        verify(evasysMockService, never()).insertCourse(trainingData);
    }

    @Test
    public void testThatCourseIsInsertedIfCourseDoesNotExist() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.courseExists(anyInt())).thenReturn(false);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, never()).updateCourse(trainingData);
        verify(evasysMockService, times(1)).insertCourse(trainingData);
    }

    @Test
    public void testThatMultipleTrainingsCanBeProcessed() {
        ZLSOSTEVASYSRFC trainingData1 = createTrainingData("1", "1", "1");
        ZLSOSTEVASYSRFC trainingData2 = createTrainingData("2", "2", "2");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData1, trainingData2);

        when(evasysMockService.trainerExists(anyString(), anyInt())).thenReturn(false);
        when(evasysMockService.courseExists(anyInt())).thenReturn(false);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, never()).updateTrainer(any());
        verify(evasysMockService, times(2)).insertTrainer(any());
        verify(evasysMockService, never()).updateCourse(any());
        verify(evasysMockService, times(2)).insertCourse(any());
    }

    @Test
    public void testThatNotifyErrorIsCalledWhenTrainerProcessingFails() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists(anyString(), anyInt())).thenThrow(new EvasysException("Trainer error"));

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(mailNotificationService, times(1)).notifyError(
                eq("Processing failed"),
                eq("Trainer error"),
                any(EvasysException.class),
                eq(trainingData));
    }

    @Test
    public void testNotifyErrorIsCalledWhenCourseProcessingFails() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists(anyString(), anyInt())).thenReturn(true);
        when(evasysMockService.courseExists(anyInt())).thenThrow(new EvasysException("Course error"));

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(mailNotificationService, times(1)).notifyError(
                eq("Processing failed"),
                eq("Course error"),
                any(EvasysException.class),
                eq(trainingData));
    }

    @Test
    public void testThatCourseIsUpdatedWhenInsertFailsButCourseExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists(anyString(), anyInt())).thenReturn(true);
        when(evasysMockService.courseExists(1)).thenReturn(false).thenReturn(true);
        doThrow(new EvasysException("Course already exists")).when(evasysMockService).insertCourse(trainingData);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, times(1)).insertCourse(trainingData);
        verify(evasysMockService, times(1)).updateCourse(trainingData);
        verify(mailNotificationService, never()).notifyError(anyString(), anyString(), any(), any());
    }

    @Test
    public void testThatCourseErrorIsPropagatedWhenInsertFailsAndCourseDoesNotExist() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists(anyString(), anyInt())).thenReturn(true);
        when(evasysMockService.courseExists(1)).thenReturn(false);
        doThrow(new EvasysException("Insert failed")).when(evasysMockService).insertCourse(trainingData);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, times(1)).insertCourse(trainingData);
        verify(evasysMockService, never()).updateCourse(trainingData);
        verify(mailNotificationService, times(1)).notifyError(
                eq("Processing failed"),
                eq("Insert failed"),
                any(EvasysException.class),
                eq(trainingData));
    }

    @Test
    public void testThatTrainerIsUpdatedWhenInsertFailsButTrainerExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists("1", 1)).thenReturn(false).thenReturn(true);
        when(evasysMockService.courseExists(anyInt())).thenReturn(true);
        doThrow(new EvasysException("Trainer already exists")).when(evasysMockService).insertTrainer(trainingData);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, times(1)).insertTrainer(trainingData);
        verify(evasysMockService, times(1)).updateTrainer(trainingData);
        verify(mailNotificationService, never()).notifyError(anyString(), anyString(), any(), any());
    }

    @Test
    public void testThatTrainerErrorIsPropagatedWhenInsertFailsAndTrainerDoesNotExist() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        when(evasysMockService.trainerExists("1", 1)).thenReturn(false);
        doThrow(new EvasysException("Insert failed")).when(evasysMockService).insertTrainer(trainingData);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, times(1)).insertTrainer(trainingData);
        verify(evasysMockService, never()).updateTrainer(trainingData);
        verify(mailNotificationService, times(1)).notifyError(
                eq("Processing failed"),
                eq("Insert failed"),
                any(EvasysException.class),
                eq(trainingData));
    }

    @Test
    public void testThatSecondaryTrainerIsUpdatedWhenInsertFailsButTrainerExists() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        trainingData.setSEKTRAINERID("2");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        SecondaryTrainer secondaryTrainer = new SecondaryTrainer(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        when(evasysMockService.trainerExists("1", 1)).thenReturn(true);
        when(evasysMockService.trainerExists("2", 1)).thenReturn(false).thenReturn(true);
        when(evasysMockService.extractSecondaryTrainers(trainingData))
                .thenReturn(List.of(secondaryTrainer));
        when(evasysMockService.courseExists(anyInt())).thenReturn(true);
        doThrow(new EvasysException("Trainer already exists"))
                .when(evasysMockService).insertSecondaryTrainer(trainingData, secondaryTrainer);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, times(1)).insertSecondaryTrainer(trainingData, secondaryTrainer);
        verify(evasysMockService, times(1)).updateSecondaryTrainer(secondaryTrainer);
        verify(mailNotificationService, never()).notifyError(anyString(), anyString(), any(), any());
    }

    @Test
    public void testThatSecondaryTrainerErrorIsPropagatedWhenInsertFailsAndTrainerDoesNotExist() {
        ZLSOSTEVASYSRFC trainingData = createTrainingData("1", "1", "1");
        trainingData.setSEKTRAINERID("2");
        ZLSOEVASYSRFC trainingRequest = createRequestWithItems(trainingData);

        SecondaryTrainer secondaryTrainer = new SecondaryTrainer(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        when(evasysMockService.trainerExists("1", 1)).thenReturn(true);
        when(evasysMockService.trainerExists("2", 1)).thenReturn(false);
        when(evasysMockService.extractSecondaryTrainers(trainingData))
                .thenReturn(List.of(secondaryTrainer));
        doThrow(new EvasysException("Insert failed"))
                .when(evasysMockService).insertSecondaryTrainer(trainingData, secondaryTrainer);

        trainingProcessorService.processTrainingRequest(trainingRequest);

        verify(evasysMockService, times(1)).insertSecondaryTrainer(trainingData, secondaryTrainer);
        verify(evasysMockService, never()).updateSecondaryTrainer(secondaryTrainer);
        verify(mailNotificationService, times(1)).notifyError(
                eq("Processing failed"),
                eq("Insert failed"),
                any(EvasysException.class),
                eq(trainingData));
    }
}
