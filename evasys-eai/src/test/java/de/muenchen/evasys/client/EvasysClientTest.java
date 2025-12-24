package de.muenchen.evasys.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvasysException;
import de.muenchen.evasys.mapper.SapEvasysMapper;
import de.muenchen.evasys.model.SecondaryTrainer;
import jakarta.xml.ws.Holder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wsdl.soapserver_v100.Course;
import wsdl.soapserver_v100.CourseIdType;
import wsdl.soapserver_v100.CourseList;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.SoapfaultMessage;
import wsdl.soapserver_v100.TSoapfault;
import wsdl.soapserver_v100.Unit;
import wsdl.soapserver_v100.UnitList;
import wsdl.soapserver_v100.User;
import wsdl.soapserver_v100.UserIdType;
import wsdl.soapserver_v100.UserList;

@ExtendWith(MockitoExtension.class)
public class EvasysClientTest {

    @Mock
    private SoapPort soapPortMock;

    private final SapEvasysMapper mapper = Mappers.getMapper(SapEvasysMapper.class);

    private EvasysClient evasysClient;

    @BeforeEach
    public void setup() {
        evasysClient = new EvasysClient(soapPortMock, mapper);
    }

    @Test
    public void testGetSubunitsReturnsUnitList() throws Exception {
        Unit mockedUnit = new Unit();
        UnitList mockedResponse = new UnitList();
        mockedResponse.getUnits().add(mockedUnit);

        when(soapPortMock.getSubunits()).thenReturn(mockedResponse);

        UnitList result = evasysClient.getSubunits();

        assertEquals(1, result.getUnits().size());
        assertEquals(mockedUnit, result.getUnits().getFirst());
    }

    @Test
    public void testGetUsersBySubunitReturnsUserList() throws Exception {
        User mockedUser = new User();
        UserList mockedResponse = new UserList();
        mockedResponse.getUsers().add(mockedUser);

        when(soapPortMock.getUsersBySubunit(
                anyInt(),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedResponse);

        UserList result = evasysClient.getUsersBySubunit(1);

        assertEquals(1, result.getUsers().size());
        assertEquals(mockedUser, result.getUsers().getFirst());
    }

    @Test
    public void testGetUsersBySubunitThrowsExceptionWhenNoUsersFound() throws Exception {
        TSoapfault faultresponse = new TSoapfault();
        faultresponse.setSErrorMessage("ERR_305");
        SoapfaultMessage soapfaultMessage = new SoapfaultMessage("No users found", faultresponse);

        when(soapPortMock.getUsersBySubunit(
                anyInt(),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenThrow(soapfaultMessage);

        EvasysException exception = assertThrows(EvasysException.class, () -> evasysClient.getUsersBySubunit(1));

        assertEquals("No users found in the given subunit", exception.getMessage());
    }

    @Test
    public void testGetUserReturnsUser() throws Exception {
        User mockedUser = new User();
        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                any(UserIdType.class),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserList);

        User result = evasysClient.getUser("1");

        Assertions.assertEquals(mockedUser, result);
    }

    @Test
    public void testGetUserThrowsExceptionWhenUserNotFound() throws Exception {
        String userId = "999";

        TSoapfault faultresponse = new TSoapfault();
        faultresponse.setSErrorMessage("ERR_302");
        SoapfaultMessage soapfaultMessage = new SoapfaultMessage("User not found", faultresponse);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                any(UserIdType.class),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenThrow(soapfaultMessage);

        EvasysException exception = assertThrows(EvasysException.class, () -> evasysClient.getUser(userId));

        assertEquals("No user found for the given id " + userId, exception.getMessage());
    }

    @Test
    public void testGetCourseReturnsCourse() throws Exception {
        Course mockedCourse = new Course();
        CourseList mockedCourseList = new CourseList();
        mockedCourseList.getCourses().add(mockedCourse);

        when(soapPortMock.getCourse(
                anyString(),
                any(CourseIdType.class),
                eq(false),
                eq(false)))
                .thenReturn(mockedCourse);

        Course result = evasysClient.getCourse(1);

        assertEquals(mockedCourse, result);
    }

    @Test
    public void testGetCourseThrowsExceptionWhenCourseNotFound() throws Exception {
        int courseId = 999;

        TSoapfault faultresponse = new TSoapfault();
        faultresponse.setSErrorMessage("ERR_312");
        SoapfaultMessage soapfaultMessage = new SoapfaultMessage("Course not found", faultresponse);

        when(soapPortMock.getCourse(
                eq(String.valueOf(courseId)),
                eq(CourseIdType.PUBLIC),
                eq(false),
                eq(false)))
                .thenThrow(soapfaultMessage);

        EvasysException exception = assertThrows(EvasysException.class, () -> evasysClient.getCourse(courseId));

        assertEquals("No course found for the given id " + courseId, exception.getMessage());
    }

    @Test
    public void testThatTrainerIsExistingReturnsTrue() throws Exception {
        String trainerId = "1";
        User mockedUser = new User();
        mockedUser.setMSExternalId(String.valueOf(trainerId));
        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        when(soapPortMock.getUsersBySubunit(
                anyInt(),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserList);

        boolean result = evasysClient.isTrainerExisting(trainerId, 1);

        assertTrue(result);
    }

    @Test
    public void testThatTrainerIsNotExistingReturnsFalse() throws Exception {
        String trainerId = "1";
        User emptyUser = new User();
        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(emptyUser);

        when(soapPortMock.getUsersBySubunit(
                anyInt(),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserList);

        boolean result = evasysClient.isTrainerExisting(trainerId, 1);

        assertFalse(result);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenUpdatingTrainer() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAINER1ID("1");
        trainingData.setTRAINER1TITEL("Dr.");
        trainingData.setTRAINER1VNAME("Max");
        trainingData.setTRAINER1NNAME("Mustermann");
        trainingData.setTRAINER1MAIL("max@example.com");
        trainingData.setTEILBEREICHID("1");
        trainingData.setTRAINERGESCHL("1");

        User mockedUser = new User();
        mockedUser.setMNId(11);
        UserList mockedResponse = new UserList();
        mockedResponse.getUsers().add(mockedUser);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedResponse);

        evasysClient.updateTrainer(trainingData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<User>> captor = ArgumentCaptor.forClass(Holder.class);
        verify(soapPortMock).updateUser(captor.capture());

        User captured = captor.getValue().value;

        assertEquals(11, captured.getMNId());
        assertEquals("1", captured.getMSExternalId());
        assertEquals("Dr.", captured.getMSTitle());
        assertEquals("Max", captured.getMSFirstName());
        assertEquals("Mustermann", captured.getMSSurName());
        assertEquals("max@example.com", captured.getMSEmail());
        assertEquals(1, captured.getMNFbid());
        assertEquals(1, captured.getMNAddressId());
    }

    @Test
    public void shouldUpdateAllUsersWithSameExternalIdWhenUpdatingTrainer() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAINER1ID("1");
        trainingData.setTRAINER1TITEL("Dr.");
        trainingData.setTRAINER1VNAME("Max");
        trainingData.setTRAINER1NNAME("Mustermann");
        trainingData.setTRAINER1MAIL("max@example.com");
        trainingData.setTEILBEREICHID("1");
        trainingData.setTRAINERGESCHL("1");

        User mockedUser1 = new User();
        mockedUser1.setMNId(11);
        mockedUser1.setMSExternalId("1");
        User mockedUser2 = new User();
        mockedUser2.setMNId(22);
        mockedUser2.setMSExternalId("1");
        User mockedUser3 = new User();
        mockedUser3.setMNId(33);
        mockedUser3.setMSExternalId("1");

        UserList mockedResponse = new UserList();
        mockedResponse.getUsers().add(mockedUser1);
        mockedResponse.getUsers().add(mockedUser2);
        mockedResponse.getUsers().add(mockedUser3);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedResponse);

        evasysClient.updateTrainer(trainingData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<User>> captor = ArgumentCaptor.forClass(Holder.class);
        verify(soapPortMock, org.mockito.Mockito.times(3)).updateUser(captor.capture());

        List<Holder<User>> allCaptured = captor.getAllValues();
        assertEquals(3, allCaptured.size());

        // Verify all users were updated with the same data
        for (Holder<User> holder : allCaptured) {
            User captured = holder.value;
            assertEquals("1", captured.getMSExternalId());
            assertEquals("Dr.", captured.getMSTitle());
            assertEquals("Max", captured.getMSFirstName());
            assertEquals("Mustermann", captured.getMSSurName());
            assertEquals("max@example.com", captured.getMSEmail());
            assertEquals(1, captured.getMNFbid());
            assertEquals(1, captured.getMNAddressId());
        }

        // Verify that all three different user IDs were updated
        List<Integer> updatedIds = allCaptured.stream()
                .map(h -> h.value.getMNId())
                .sorted()
                .toList();
        assertEquals(List.of(11, 22, 33), updatedIds);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenInsertingTrainer() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAINER1ID("1");
        trainingData.setTRAINER1TITEL("Dr.");
        trainingData.setTRAINER1VNAME("Max");
        trainingData.setTRAINER1NNAME("Mustermann");
        trainingData.setTRAINER1MAIL("max@example.com");
        trainingData.setTEILBEREICHID("1");
        trainingData.setTRAINERGESCHL("1");

        evasysClient.insertTrainer(trainingData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<User>> captor = ArgumentCaptor.forClass(Holder.class);
        verify(soapPortMock).insertUser(captor.capture());

        User captured = captor.getValue().value;

        assertEquals("1", captured.getMSExternalId());
        assertEquals("Dr.", captured.getMSTitle());
        assertEquals("Max", captured.getMSFirstName());
        assertEquals("Mustermann", captured.getMSSurName());
        assertEquals("max@example.com", captured.getMSEmail());
        assertEquals(1, captured.getMNFbid());
        assertEquals(1, captured.getMNAddressId());
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenUpdatingSecondaryTrainer() throws Exception {
        SecondaryTrainer secondaryTrainer = new SecondaryTrainer(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        User mockedUser = new User();
        mockedUser.setMNId(22);
        UserList mockedResponse = new UserList();
        mockedResponse.getUsers().add(mockedUser);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedResponse);

        evasysClient.updateSecondaryTrainer(secondaryTrainer);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<User>> captor = ArgumentCaptor.forClass(Holder.class);
        verify(soapPortMock).updateUser(captor.capture());

        User captured = captor.getValue().value;

        assertEquals(22, captured.getMNId());
        assertEquals("2", captured.getMSExternalId());
        assertEquals("Prof.", captured.getMSTitle());
        assertEquals("Erika", captured.getMSFirstName());
        assertEquals("Musterfrau", captured.getMSSurName());
        assertEquals("erika@example.com", captured.getMSEmail());
        assertEquals(2, captured.getMNAddressId());
    }

    @Test
    public void shouldUpdateAllUsersWithSameExternalIdWhenUpdatingSecondaryTrainer() throws Exception {
        SecondaryTrainer secondaryTrainer = new SecondaryTrainer(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        User mockedUser1 = new User();
        mockedUser1.setMNId(22);
        mockedUser1.setMSExternalId("2");
        User mockedUser2 = new User();
        mockedUser2.setMNId(44);
        mockedUser2.setMSExternalId("2");
        User mockedUser3 = new User();
        mockedUser3.setMNId(66);
        mockedUser3.setMSExternalId("2");

        UserList mockedResponse = new UserList();
        mockedResponse.getUsers().add(mockedUser1);
        mockedResponse.getUsers().add(mockedUser2);
        mockedResponse.getUsers().add(mockedUser3);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedResponse);

        evasysClient.updateSecondaryTrainer(secondaryTrainer);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<User>> captor = ArgumentCaptor.forClass(Holder.class);
        verify(soapPortMock, org.mockito.Mockito.times(3)).updateUser(captor.capture());

        List<Holder<User>> allCaptured = captor.getAllValues();
        assertEquals(3, allCaptured.size());

        // Verify all users were updated with the same data
        for (Holder<User> holder : allCaptured) {
            User captured = holder.value;
            assertEquals("2", captured.getMSExternalId());
            assertEquals("Prof.", captured.getMSTitle());
            assertEquals("Erika", captured.getMSFirstName());
            assertEquals("Musterfrau", captured.getMSSurName());
            assertEquals("erika@example.com", captured.getMSEmail());
            assertEquals(2, captured.getMNAddressId());
        }

        // Verify that all three different user IDs were updated
        List<Integer> updatedIds = allCaptured.stream()
                .map(h -> h.value.getMNId())
                .sorted()
                .toList();
        assertEquals(List.of(22, 44, 66), updatedIds);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenInsertingSecondaryTrainer() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTEILBEREICHID("1");

        SecondaryTrainer secondaryTrainer = new SecondaryTrainer(
                "2",
                "2",
                "Prof.",
                "Erika",
                "Musterfrau",
                "erika@example.com");

        evasysClient.insertSecondaryTrainer(trainingData, secondaryTrainer);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<User>> captor = ArgumentCaptor.forClass(Holder.class);
        verify(soapPortMock).insertUser(captor.capture());

        User captured = captor.getValue().value;

        assertEquals("2", captured.getMSExternalId());
        assertEquals("Prof.", captured.getMSTitle());
        assertEquals("Erika", captured.getMSFirstName());
        assertEquals("Musterfrau", captured.getMSSurName());
        assertEquals("erika@example.com", captured.getMSEmail());
        assertEquals(1, captured.getMNFbid());
        assertEquals(2, captured.getMNAddressId());
    }

    @Test
    public void testThatCourseIsExistingReturnsTrue() throws Exception {
        int courseId = 1;
        Course mockedCourse = new Course();
        mockedCourse.setMNCourseId(courseId);

        when(soapPortMock.getCourse(
                eq(String.valueOf(courseId)),
                eq(CourseIdType.PUBLIC),
                eq(false),
                eq(false)))
                .thenReturn(mockedCourse);

        boolean result = evasysClient.isCourseExisting(courseId);

        assertTrue(result);
    }

    @Test
    public void testThatCourseIsNotExistingReturnsFalse() throws Exception {
        int courseId = 1;

        TSoapfault faultresponse = new TSoapfault();
        faultresponse.setSErrorMessage("ERR_312");
        SoapfaultMessage soapfaultMessage = new SoapfaultMessage("Course not found", faultresponse);

        when(soapPortMock.getCourse(
                eq(String.valueOf(courseId)),
                eq(CourseIdType.PUBLIC),
                eq(false),
                eq(false)))
                .thenThrow(soapfaultMessage);

        boolean result = evasysClient.isCourseExisting(courseId);

        assertFalse(result);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenUpdatingCourse() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAININGART("1");
        trainingData.setTRAINERGESCHL("männlich");
        trainingData.setTRAINEROBJTYP("INTERNAL");
        trainingData.setFIRMA("Test-Firma");
        trainingData.setTEILBEREICHID("1");
        trainingData.setTRAININGBEGINN("2025-10-01");
        trainingData.setTRAININGENDE("2025-10-05");
        trainingData.setVAVNAME("Max");
        trainingData.setVANNAME("Mustermann");
        trainingData.setSBVNAME("Erika");
        trainingData.setSBNNAME("Musterfrau");
        trainingData.setTRAININGDAUERTAGE("5");
        trainingData.setTRAININGDAUERSTD("40");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("33");

        User mockedUser = new User();
        mockedUser.setMNId(44);
        UserList mockedUserListResponse = new UserList();
        mockedUserListResponse.getUsers().add(mockedUser);
        Course mockedCourseResponse = new Course();
        mockedCourseResponse.setMNCourseId(55);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserListResponse);

        when(soapPortMock.getCourse(
                anyString(),
                eq(CourseIdType.PUBLIC),
                eq(false),
                eq(false)))
                .thenReturn(mockedCourseResponse);

        evasysClient.updateCourse(trainingData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<Course>> captor = ArgumentCaptor.forClass(Holder.class);

        verify(soapPortMock).updateCourse(captor.capture(), eq(false));

        Holder<Course> capturedHolder = captor.getValue();
        Course captured = capturedHolder.value;

        assertEquals(55, captured.getMNCourseId());
        assertEquals(1, captured.getMNCourseType());
        assertEquals("11", captured.getMSPubCourseId());
        assertEquals(44, captured.getMNUserId());
        assertEquals(33, captured.getMNFbid());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualJson = mapper.readTree(captured.getMSCustomFieldsJSON());
        JsonNode expectedJson = mapper.readTree("""
                {
                "1": "männlich",
                "2": "INTERNAL",
                "3": "Test-Firma",
                "4": "",
                "5": "",
                "6": "33",
                "7": "2025-10-01",
                "8": "2025-10-05",
                "9": "Max Mustermann",
                "10": "Erika Musterfrau",
                "11": "5",
                "12": "40"
                }
                """);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenInsertingCourse() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAININGSTYPKUERZEL("Test");
        trainingData.setTRAININGTITEL("Test-Kurs");
        trainingData.setTRAININGRAUM("1.20");
        trainingData.setTRAININGART("1");
        trainingData.setTRAININGTNANZAHL("1");
        trainingData.setTRAINERGESCHL("männlich");
        trainingData.setTRAINEROBJTYP("INTERNAL");
        trainingData.setFIRMA("Test-Firma");
        trainingData.setTEILBEREICHID("1");
        trainingData.setTRAININGBEGINN("2025-10-01");
        trainingData.setTRAININGENDE("2025-10-05");
        trainingData.setVAVNAME("Max");
        trainingData.setVANNAME("Mustermann");
        trainingData.setSBVNAME("Erika");
        trainingData.setSBNNAME("Musterfrau");
        trainingData.setTRAININGDAUERTAGE("5");
        trainingData.setTRAININGDAUERSTD("40");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("33");

        User mockedUser = new User();
        mockedUser.setMNId(44);
        UserList mockedResponse = new UserList();
        mockedResponse.getUsers().add(mockedUser);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedResponse);

        evasysClient.insertCourse(trainingData);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(soapPortMock).insertCourse(captor.capture());

        Course captured = captor.getValue();

        assertEquals("11", captured.getMSPubCourseId());
        assertEquals("Test", captured.getMSProgramOfStudy());
        assertEquals("Test-Kurs", captured.getMSCourseTitle());
        assertEquals("1.20", captured.getMSRoom());
        assertEquals(44, captured.getMNUserId());
        assertEquals(33, captured.getMNFbid());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualJson = mapper.readTree(captured.getMSCustomFieldsJSON());
        JsonNode expectedJson = mapper.readTree("""
                {
                "1": "männlich",
                "2": "INTERNAL",
                "3": "Test-Firma",
                "4": "",
                "5": "",
                "6": "33",
                "7": "2025-10-01",
                "8": "2025-10-05",
                "9": "Max Mustermann",
                "10": "Erika Musterfrau",
                "11": "5",
                "12": "40"
                }
                """);
        assertEquals(expectedJson, actualJson);
    }
}
