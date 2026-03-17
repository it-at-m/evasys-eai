package de.muenchen.evasys.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvasysException;
import de.muenchen.evasys.mapper.SapEvasysMapper;
import jakarta.xml.ws.Holder;
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
import wsdl.soapserver_v100.User;
import wsdl.soapserver_v100.UserIdType;
import wsdl.soapserver_v100.UserList;

@ExtendWith(MockitoExtension.class)
public class EvasysCourseClientTest {

    @Mock
    private SoapPort soapPortMock;

    private final SapEvasysMapper mapper = Mappers.getMapper(SapEvasysMapper.class);
    private final SoapExecutor soapExecutor = new SoapExecutor();

    private EvasysUserClient evasysUserClient;
    private EvasysCourseClient evasysCourseClient;

    @BeforeEach
    void setup() {
        evasysUserClient = new EvasysUserClient(
                soapPortMock,
                soapExecutor,
                mapper);
        evasysCourseClient = new EvasysCourseClient(
                soapPortMock,
                soapExecutor,
                mapper,
                evasysUserClient);
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

        Course result = evasysCourseClient.getCourse(1);

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

        EvasysException exception = assertThrows(EvasysException.class, () -> evasysCourseClient.getCourse(courseId));

        assertEquals("No course found for the given id " + courseId, exception.getMessage());
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

        boolean result = evasysCourseClient.isCourseExisting(courseId);

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

        boolean result = evasysCourseClient.isCourseExisting(courseId);

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
        mockedUser.setMNFbid(33);
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

        evasysCourseClient.updateCourse(trainingData);

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
        assertEquals(true, captured.isHasAnonymousParticipants());

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
        mockedUser.setMNFbid(33);
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

        evasysCourseClient.insertCourse(trainingData);

        org.mockito.ArgumentCaptor<Course> captor = org.mockito.ArgumentCaptor.forClass(Course.class);
        verify(soapPortMock).insertCourse(captor.capture());

        Course captured = captor.getValue();

        assertEquals("11", captured.getMSPubCourseId());
        assertEquals("Test", captured.getMSProgramOfStudy());
        assertEquals("Test-Kurs", captured.getMSCourseTitle());
        assertEquals("1.20", captured.getMSRoom());
        assertEquals(44, captured.getMNUserId());
        assertEquals(33, captured.getMNFbid());
        assertEquals(true, captured.isHasAnonymousParticipants());

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
    public void shouldSelectCorrectUserBySubunitIdWhenUpdatingCourse() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("33");

        // Create multiple users with same external ID but different subunit IDs
        User user1 = new User();
        user1.setMNId(44);
        user1.setMSExternalId("22");
        user1.setMNFbid(11); // Different subunit

        User user2 = new User();
        user2.setMNId(55);
        user2.setMSExternalId("22");
        user2.setMNFbid(33); // Matches TEILBEREICHID

        User user3 = new User();
        user3.setMNId(66);
        user3.setMSExternalId("22");
        user3.setMNFbid(22); // Different subunit

        UserList mockedUserListResponse = new UserList();
        mockedUserListResponse.getUsers().add(user1);
        mockedUserListResponse.getUsers().add(user2);
        mockedUserListResponse.getUsers().add(user3);

        Course mockedCourseResponse = new Course();
        mockedCourseResponse.setMNCourseId(77);

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

        evasysCourseClient.updateCourse(trainingData);

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<Holder<Course>> captor = org.mockito.ArgumentCaptor.forClass(Holder.class);
        verify(soapPortMock).updateCourse(captor.capture(), eq(false));

        Course captured = captor.getValue().value;

        assertEquals(55, captured.getMNUserId());
    }

    @Test
    public void shouldSelectCorrectUserBySubunitIdWhenInsertingCourse() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("33");

        // Create multiple users with same external ID but different subunit IDs
        User user1 = new User();
        user1.setMNId(44);
        user1.setMSExternalId("22");
        user1.setMNFbid(11); // Different subunit

        User user2 = new User();
        user2.setMNId(55);
        user2.setMSExternalId("22");
        user2.setMNFbid(33); // Matches TEILBEREICHID

        User user3 = new User();
        user3.setMNId(66);
        user3.setMSExternalId("22");
        user3.setMNFbid(22); // Different subunit

        UserList mockedUserListResponse = new UserList();
        mockedUserListResponse.getUsers().add(user1);
        mockedUserListResponse.getUsers().add(user2);
        mockedUserListResponse.getUsers().add(user3);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserListResponse);

        evasysCourseClient.insertCourse(trainingData);

        org.mockito.ArgumentCaptor<Course> captor = org.mockito.ArgumentCaptor.forClass(Course.class);
        verify(soapPortMock).insertCourse(captor.capture());

        Course captured = captor.getValue();

        assertEquals(55, captured.getMNUserId());
    }

    @Test
    public void shouldThrowExceptionWhenNoUserMatchesSubunitId() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("33");

        // Create users with same external ID but none matching the subunit ID
        User user1 = new User();
        user1.setMNId(44);
        user1.setMSExternalId("22");
        user1.setMNFbid(11); // Different subunit

        User user2 = new User();
        user2.setMNId(55);
        user2.setMSExternalId("22");
        user2.setMNFbid(22); // Different subunit

        UserList mockedUserListResponse = new UserList();
        mockedUserListResponse.getUsers().add(user1);
        mockedUserListResponse.getUsers().add(user2);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserListResponse);

        EvasysException exception = assertThrows(EvasysException.class, () -> evasysCourseClient.updateCourse(trainingData));
        assertTrue(exception.getMessage().contains("No user found for external ID 22 and subunit ID 33"));
    }

    @Test
    public void shouldThrowExceptionWhenTeilbereichIdIsMissing() {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID(null);

        EvasysException exception = assertThrows(EvasysException.class, () -> evasysCourseClient.updateCourse(trainingData));
        assertTrue(exception.getMessage().contains("TEILBEREICHID must not be empty"));
    }

    @Test
    public void shouldThrowExceptionWhenTeilbereichIdIsBlank() {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("");

        EvasysException exception = assertThrows(EvasysException.class, () -> evasysCourseClient.insertCourse(trainingData));
        assertTrue(exception.getMessage().contains("TEILBEREICHID must not be empty"));
    }

    @Test
    public void shouldTreatAlreadyExistingCourseAsSuccessWhenInserting() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("33");

        User mockedUser = new User();
        mockedUser.setMNId(44);
        mockedUser.setMNFbid(33);

        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserList);

        TSoapfault fault = new TSoapfault();
        fault.setSErrorMessage("ERR_313");
        SoapfaultMessage soapfaultMessage = new SoapfaultMessage("Already exists", fault);

        doThrow(soapfaultMessage)
                .when(soapPortMock)
                .insertCourse(any(Course.class));

        evasysCourseClient.insertCourse(trainingData);

        verify(soapPortMock).insertCourse(any(Course.class));
    }

    @Test
    public void shouldTreatAlreadyExistingCourseAsSuccessWhenUpdating() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("33");

        User mockedUser = new User();
        mockedUser.setMNId(44);
        mockedUser.setMNFbid(33);

        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        Course existingCourse = new Course();
        existingCourse.setMNCourseId(55);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserList);

        when(soapPortMock.getCourse(
                anyString(),
                eq(CourseIdType.PUBLIC),
                eq(false),
                eq(false)))
                .thenReturn(existingCourse);

        TSoapfault fault = new TSoapfault();
        fault.setSErrorMessage("ERR_313");
        SoapfaultMessage soapfaultMessage = new SoapfaultMessage("Already exists", fault);

        org.mockito.Mockito.doThrow(soapfaultMessage)
                .when(soapPortMock)
                .updateCourse(any(Holder.class), eq(false));

        evasysCourseClient.updateCourse(trainingData);

        verify(soapPortMock).updateCourse(any(Holder.class), eq(false));
    }

    @Test
    public void shouldRethrowExceptionWhenInsertFailsWithDifferentError() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("33");

        User mockedUser = new User();
        mockedUser.setMNId(44);
        mockedUser.setMNFbid(33);

        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserList);

        TSoapfault fault = new TSoapfault();
        fault.setSErrorMessage("ERR_999");
        SoapfaultMessage soapfaultMessage = new SoapfaultMessage("Some other error", fault);

        org.mockito.Mockito.doThrow(soapfaultMessage)
                .when(soapPortMock)
                .insertCourse(any(Course.class));

        assertThrows(EvasysException.class,
                () -> evasysCourseClient.insertCourse(trainingData));
    }

    @Test
    public void shouldRethrowExceptionWhenUpdateFailsWithDifferentError() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
        trainingData.setTRAINER1ID("22");
        trainingData.setTEILBEREICHID("33");

        User mockedUser = new User();
        mockedUser.setMNId(44);
        mockedUser.setMNFbid(33);

        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        Course existingCourse = new Course();
        existingCourse.setMNCourseId(55);

        when(soapPortMock.getUserByIdConsiderExternalID(
                anyString(),
                eq(UserIdType.EXTERNAL),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserList);

        when(soapPortMock.getCourse(
                anyString(),
                eq(CourseIdType.PUBLIC),
                eq(false),
                eq(false)))
                .thenReturn(existingCourse);

        TSoapfault fault = new TSoapfault();
        fault.setSErrorMessage("ERR_999");
        SoapfaultMessage soapfaultMessage = new SoapfaultMessage("Some other error", fault);

        org.mockito.Mockito.doThrow(soapfaultMessage)
                .when(soapPortMock)
                .updateCourse(any(Holder.class), eq(false));

        assertThrows(EvasysException.class,
                () -> evasysCourseClient.updateCourse(trainingData));
    }
}
