package de.muenchen.evasys.client;

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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import wsdl.soapserver_v100.Course;
import wsdl.soapserver_v100.CourseIdType;
import wsdl.soapserver_v100.CourseList;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.Unit;
import wsdl.soapserver_v100.UnitList;
import wsdl.soapserver_v100.User;
import wsdl.soapserver_v100.UserIdType;
import wsdl.soapserver_v100.UserList;

@SpringBootTest
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

        Mockito.when(soapPortMock.getSubunits()).thenReturn(mockedResponse);

        UnitList result = evaSysClient.getSubunits();

        Assertions.assertEquals(1, result.getUnits().size());
        Assertions.assertEquals(mockedUnit, result.getUnits().getFirst());
    }

    @Test
    public void testGetUsersBySubunitReturnsUserList() throws Exception {
        User mockedUser = new User();
        UserList mockedResponse = new UserList();
        mockedResponse.getUsers().add(mockedUser);

        Mockito.when(soapPortMock.getUsersBySubunit(
                Mockito.anyInt(),
                Mockito.eq(false),
                Mockito.eq(false),
                Mockito.eq(false),
                Mockito.eq(false)))
                .thenReturn(mockedResponse);

        UserList result = evaSysClient.getUsersBySubunit(1);

        Assertions.assertEquals(1, result.getUsers().size());
        Assertions.assertEquals(mockedUser, result.getUsers().getFirst());
    }

    @Test
    public void testGetUserReturnsUser() throws Exception {
        User mockedUser = new User();
        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        Mockito.when(soapPortMock.getUserByIdConsiderExternalID(
                Mockito.anyString(),
                Mockito.any(UserIdType.class),
                Mockito.eq(false),
                Mockito.eq(false),
                Mockito.eq(false),
                Mockito.eq(false)))
                .thenReturn(mockedUserList);

        User result = evaSysClient.getUser(1);

        Assertions.assertEquals(mockedUser, result);
    }

    @Test
    public void testGetCourseReturnsCourse() throws Exception {
        Course mockedCourse = new Course();
        CourseList mockedCourseList = new CourseList();
        mockedCourseList.getCourses().add(mockedCourse);

        Mockito.when(soapPortMock.getCourse(
                Mockito.anyString(),
                Mockito.any(CourseIdType.class),
                Mockito.eq(false),
                Mockito.eq(false)))
                .thenReturn(mockedCourse);

        Course result = evaSysClient.getCourse(1);

        Assertions.assertEquals(mockedCourse, result);
    }

    @Test
    public void testThatTrainerIsExisitingReturnsTrue() throws Exception {
        int trainerId = 1;
        User mockedUser = new User();
        mockedUser.setMNId(trainerId);
        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(mockedUser);

        Mockito.when(soapPortMock.getUsersBySubunit(
                Mockito.anyInt(),
                Mockito.eq(false),
                Mockito.eq(false),
                Mockito.eq(false),
                Mockito.eq(false)))
                .thenReturn(mockedUserList);

        boolean result = evaSysClient.isTrainerExisting(trainerId, 1);

        Assertions.assertTrue(result);
    }

    @Test
    public void testThatTrainerIsNotExisitingReturnsFalse() throws Exception {
        int trainerId = 1;
        User emptyUser = new User();
        UserList mockedUserList = new UserList();
        mockedUserList.getUsers().add(emptyUser);

        Mockito.when(soapPortMock.getUsersBySubunit(
                Mockito.anyInt(),
                Mockito.eq(false),
                Mockito.eq(false),
                Mockito.eq(false),
                Mockito.eq(false)))
                .thenReturn(mockedUserList);

        boolean result = evaSysClient.isTrainerExisting(trainerId, 1);

        Assertions.assertFalse(result);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenUpdatingTrainer() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAINER1ID("511");
        trainingData.setTRAINER1TITEL("Dr. 1");
        trainingData.setTRAINER1VNAME("Fabian");
        trainingData.setTRAINER1NNAME("Mustermann");
        trainingData.setTRAINER1MAIL("max@example.com");
        trainingData.setTEILBEREICHID("1");
        trainingData.setTRAINERGESCHL("1");

        evaSysClient.updateTrainer(trainingData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<User>> captor = ArgumentCaptor.forClass(Holder.class);
        Mockito.verify(soapPortMock).updateUser(captor.capture());

        User captured = captor.getValue().value;

        Assertions.assertEquals(511, captured.getMNId());
        Assertions.assertEquals("511", captured.getMSExternalId());
        Assertions.assertEquals("Dr. 1", captured.getMSTitle());
        Assertions.assertEquals("Fabian", captured.getMSFirstName());
        Assertions.assertEquals("Mustermann", captured.getMSSurName());
        Assertions.assertEquals("max@example.com", captured.getMSEmail());
        Assertions.assertEquals(1, captured.getMNFbid());
        Assertions.assertEquals(1, captured.getMNAddressId());
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenInsertingTrainer() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAINER1ID("999999998");
        trainingData.setTRAINER1TITEL("Dr. 1");
        trainingData.setTRAINER1VNAME("Max");
        trainingData.setTRAINER1NNAME("Testermann");
        trainingData.setTRAINER1MAIL("test@example.com");
        trainingData.setTEILBEREICHID("1");
        trainingData.setTRAINERGESCHL("1");

        evaSysClient.insertTrainer(trainingData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<User>> captor = ArgumentCaptor.forClass(Holder.class);
        Mockito.verify(soapPortMock).insertUser(captor.capture());

        User captured = captor.getValue().value;

        Assertions.assertEquals("999999998", captured.getMSExternalId());
        Assertions.assertEquals("Dr. 1", captured.getMSTitle());
        Assertions.assertEquals("Max", captured.getMSFirstName());
        Assertions.assertEquals("Testermann", captured.getMSSurName());
        Assertions.assertEquals("test@example.com", captured.getMSEmail());
        Assertions.assertEquals(1, captured.getMNFbid());
        Assertions.assertEquals(1, captured.getMNAddressId());
    }

    @Test
    public void testThatCourseIsExisitingReturnsTrue() throws Exception {
        int courseId = 1;
        Course mockedCourse = new Course();
        mockedCourse.setMNCourseId(courseId);

        Mockito.when(soapPortMock.getCourse(
                Mockito.eq(String.valueOf(courseId)),
                Mockito.eq(CourseIdType.PUBLIC),
                Mockito.eq(false),
                Mockito.eq(false)))
                .thenReturn(mockedCourse);

        boolean result = evaSysClient.isCourseExisting(courseId);

        Assertions.assertTrue(result);
    }

    @Test
    public void testThatCourseIsNotExisitingReturnsFalse() throws Exception {
        int courseId = 1;
        Course emptyCourse = new Course();

        Mockito.when(soapPortMock.getCourse(
                Mockito.eq(String.valueOf(courseId)),
                Mockito.eq(CourseIdType.PUBLIC),
                Mockito.eq(false),
                Mockito.eq(false)))
                .thenReturn(emptyCourse);

        boolean result = evaSysClient.isCourseExisting(courseId);

        Assertions.assertFalse(result);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenUpdatingCourse() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("902");
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
        trainingData.setTRAINER1ID("511");
        trainingData.setTEILBEREICHID("5");

        evaSysClient.updateCourse(trainingData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Holder<Course>> captor = ArgumentCaptor.forClass(Holder.class);

        Mockito.verify(soapPortMock).updateCourse(captor.capture(), Mockito.eq(false));

        Holder<Course> capturedHolder = captor.getValue();
        Course captured = capturedHolder.value;

        Assertions.assertEquals(902, captured.getMNCourseId());
        Assertions.assertEquals(1, captured.getMNCourseType());
        Assertions.assertEquals("902", captured.getMSPubCourseId());
        Assertions.assertEquals(511, captured.getMNUserId());
        Assertions.assertEquals(5, captured.getMNFbid());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualJson = mapper.readTree(captured.getMSCustomFieldsJSON());
        JsonNode expectedJson = mapper.readTree("""
                {
                "1": "männlich",
                "2": "INTERNAL",
                "3": "Test-Firma",
                "4": "",
                "5": "",
                "6": "5",
                "7": "2025-10-01",
                "8": "2025-10-05",
                "9": "Max Mustermann",
                "10": "Erika Musterfrau",
                "11": "5",
                "12": "40"
                }
                """);
        Assertions.assertEquals(expectedJson, actualJson);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenInsertingCourse() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAININGID("903");
        trainingData.setTRAININGSTYPKUERZEL("Test");
        trainingData.setTRAININGTITEL("Test-Kurs (903)");
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
        trainingData.setTRAINER1ID("511");
        trainingData.setTEILBEREICHID("5");

        evaSysClient.insertCourse(trainingData);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        Mockito.verify(soapPortMock).insertCourse(captor.capture());

        Course captured = captor.getValue();

        Assertions.assertEquals("903", captured.getMSPubCourseId());
        Assertions.assertEquals("Test", captured.getMSProgramOfStudy());
        Assertions.assertEquals("Test-Kurs (903)", captured.getMSCourseTitle());
        Assertions.assertEquals("1.20", captured.getMSRoom());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualJson = mapper.readTree(captured.getMSCustomFieldsJSON());
        JsonNode expectedJson = mapper.readTree("""
                {
                "1": "männlich",
                "2": "INTERNAL",
                "3": "Test-Firma",
                "4": "",
                "5": "",
                "6": "5",
                "7": "2025-10-01",
                "8": "2025-10-05",
                "9": "Max Mustermann",
                "10": "Erika Musterfrau",
                "11": "5",
                "12": "40"
                }
                """);
        Assertions.assertEquals(expectedJson, actualJson);
    }
}
