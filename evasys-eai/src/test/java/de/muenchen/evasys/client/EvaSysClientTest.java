package de.muenchen.evasys.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.configuration.EvaSysProperties;
import jakarta.xml.ws.Holder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wsdl.soapserver_v100.Course;
import wsdl.soapserver_v100.CourseIdType;
import wsdl.soapserver_v100.CourseList;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.Unit;
import wsdl.soapserver_v100.UnitList;
import wsdl.soapserver_v100.User;
import wsdl.soapserver_v100.UserIdType;
import wsdl.soapserver_v100.UserList;

@ExtendWith(MockitoExtension.class)
public class EvaSysClientTest {

    @Mock
    private SoapPort soapPortMock;

    private EvaSysClient evaSysClient;

    private EvaSysProperties evaSysProperties;

    @BeforeEach
    public void setup() {
        evaSysProperties = new EvaSysProperties("http://dummy", "user", "password");
        evaSysClient = new EvaSysClient(evaSysProperties, soapPortMock);
    }

    @Test
    public void testGetSubunitsReturnsUnitList() throws Exception {
        Unit mockedUnit = new Unit();
        UnitList mockedResponse = new UnitList();
        mockedResponse.getUnits().add(mockedUnit);

        when(soapPortMock.getSubunits()).thenReturn(mockedResponse);

        UnitList result = evaSysClient.getSubunits();

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

        UserList result = evaSysClient.getUsersBySubunit(1);

        assertEquals(1, result.getUsers().size());
        assertEquals(mockedUser, result.getUsers().getFirst());
    }

    @Test
    public void testGetUserReturnsUser() throws Exception {
        User mockedUser = new User();
        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        when(soapPortMock.getUserByIdConsiderExternalID(
                toString(),
                any(UserIdType.class),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserList);

        User result = evaSysClient.getUser(1);

        Assertions.assertEquals(mockedUser, result);
    }

    @Test
    public void testGetCourseReturnsCourse() throws Exception {
        Course mockedCourse = new Course();
        CourseList mockedCourseList = new CourseList();
        mockedCourseList.getCourses().add(mockedCourse);

        when(soapPortMock.getCourse(
                toString(),
                any(CourseIdType.class),
                eq(false),
                eq(false)))
                .thenReturn(mockedCourse);

        Course result = evaSysClient.getCourse(1);

        assertEquals(mockedCourse, result);
    }

    @Test
    public void testThatTrainerIsExisitingReturnsTrue() throws Exception {
        int trainerId = 1;
        User mockedUser = new User();
        mockedUser.setMNId(trainerId);
        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        when(soapPortMock.getUsersBySubunit(
                anyInt(),
                eq(false),
                eq(false),
                eq(false),
                eq(false)))
                .thenReturn(mockedUserList);

        boolean result = evaSysClient.isTrainerExisting(trainerId, 1);

        assertTrue(result);
    }

    @Test
    public void testThatTrainerIsNotExisitingReturnsFalse() throws Exception {
        int trainerId = 1;
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

        boolean result = evaSysClient.isTrainerExisting(trainerId, 1);

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

        evaSysClient.updateTrainer(trainingData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<User>> captor = ArgumentCaptor.forClass(Holder.class);
        verify(soapPortMock).updateUser(captor.capture());

        User captured = captor.getValue().value;

        assertEquals(1, captured.getMNId());
        assertEquals("1", captured.getMSExternalId());
        assertEquals("Dr.", captured.getMSTitle());
        assertEquals("Max", captured.getMSFirstName());
        assertEquals("Mustermann", captured.getMSSurName());
        assertEquals("max@example.com", captured.getMSEmail());
        assertEquals(1, captured.getMNFbid());
        assertEquals(1, captured.getMNAddressId());
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

        evaSysClient.insertTrainer(trainingData);

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
    public void testThatCourseIsExisitingReturnsTrue() throws Exception {
        int courseId = 1;
        Course mockedCourse = new Course();
        mockedCourse.setMNCourseId(courseId);

        when(soapPortMock.getCourse(
                eq(String.valueOf(courseId)),
                eq(CourseIdType.PUBLIC),
                eq(false),
                eq(false)))
                .thenReturn(mockedCourse);

        boolean result = evaSysClient.isCourseExisting(courseId);

        assertTrue(result);
    }

    @Test
    public void testThatCourseIsNotExisitingReturnsFalse() throws Exception {
        int courseId = 1;
        Course emptyCourse = new Course();

        when(soapPortMock.getCourse(
                eq(String.valueOf(courseId)),
                eq(CourseIdType.PUBLIC),
                eq(false),
                eq(false)))
                .thenReturn(emptyCourse);

        boolean result = evaSysClient.isCourseExisting(courseId);

        assertFalse(result);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenUpdatingCourse() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("11");
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

        evaSysClient.updateCourse(trainingData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<Course>> captor = ArgumentCaptor.forClass(Holder.class);

        verify(soapPortMock).updateCourse(captor.capture(), eq(false));

        Holder<Course> capturedHolder = captor.getValue();
        Course captured = capturedHolder.value;

        assertEquals(11, captured.getMNCourseId());
        assertEquals(1, captured.getMNCourseType());
        assertEquals("11", captured.getMSPubCourseId());
        assertEquals(22, captured.getMNUserId());
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

        evaSysClient.insertCourse(trainingData);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(soapPortMock).insertCourse(captor.capture());

        Course captured = captor.getValue();

        assertEquals("11", captured.getMSPubCourseId());
        assertEquals("Test", captured.getMSProgramOfStudy());
        assertEquals("Test-Kurs", captured.getMSCourseTitle());
        assertEquals("1.20", captured.getMSRoom());
        assertEquals(22, captured.getMNUserId());
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
