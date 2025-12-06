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
import de.muenchen.evasys.exception.EvaSysException;
import de.muenchen.evasys.mapper.SapEvaSysMapper;
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
import wsdl.soapserver_v100.Unit;
import wsdl.soapserver_v100.UnitList;
import wsdl.soapserver_v100.User;
import wsdl.soapserver_v100.UserIdType;
import wsdl.soapserver_v100.UserList;

@ExtendWith(MockitoExtension.class)
public class EvaSysClientTest {

    @Mock
    private SoapPort soapPortMock;

    private final SapEvaSysMapper mapper = Mappers.getMapper(SapEvaSysMapper.class);

    private EvaSysClient evaSysClient;

    @BeforeEach
    public void setup() {
        evaSysClient = new EvaSysClient(soapPortMock, mapper);
    }

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
                anyString(),
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
                anyString(),
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

        evaSysClient.updateTrainer(trainingData);

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
    void testReturnsEmptyListWhenNoSecondaryTrainer() {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setSEKTRAINERID(null);

        List<SecondaryTrainer> trainers = evaSysClient.extractSecondaryTrainers(trainingData);

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

        List<SecondaryTrainer> result = evaSysClient.extractSecondaryTrainers(trainingData);

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

        List<SecondaryTrainer> result = evaSysClient.extractSecondaryTrainers(trainingData);

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

        assertThrows(EvaSysException.class, () -> evaSysClient.extractSecondaryTrainers(trainingData));
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenUpdatingSecondaryTrainer() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTEILBEREICHID("1");

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

        evaSysClient.updateSecondaryTrainer(trainingData, secondaryTrainer);

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
        assertEquals(1, captured.getMNFbid());
        assertEquals(2, captured.getMNAddressId());
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

        evaSysClient.insertSecondaryTrainer(trainingData, secondaryTrainer);

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

        when(soapPortMock.getCourse(
                eq(String.valueOf(courseId)),
                eq(CourseIdType.PUBLIC),
                eq(false),
                eq(false)))
                .thenReturn(null);

        boolean result = evaSysClient.isCourseExisting(courseId);

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

        evaSysClient.updateCourse(trainingData);

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

        evaSysClient.insertCourse(trainingData);

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
