package de.muenchen.evasys.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvasysException;
import de.muenchen.evasys.mapper.SapEvasysMapper;
import de.muenchen.evasys.model.SecondaryTrainer;
import jakarta.xml.ws.Holder;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.SoapfaultMessage;
import wsdl.soapserver_v100.TSoapfault;
import wsdl.soapserver_v100.Unit;
import wsdl.soapserver_v100.UnitList;
import wsdl.soapserver_v100.User;
import wsdl.soapserver_v100.UserIdType;
import wsdl.soapserver_v100.UserList;

@ExtendWith(MockitoExtension.class)
public class EvasysUserClientTest {

    @Mock
    private SoapPort soapPortMock;

    private final SapEvasysMapper mapper = Mappers.getMapper(SapEvasysMapper.class);
    private final SoapExecutor soapExecutor = new SoapExecutor();

    private EvasysUserClient evasysUserClient;

    @BeforeEach
    void setup() {
        evasysUserClient = new EvasysUserClient(
                soapPortMock,
                soapExecutor,
                mapper);
    }

    @Test
    public void testGetSubunitsReturnsUnitList() throws Exception {
        Unit mockedUnit = new Unit();
        UnitList mockedResponse = new UnitList();
        mockedResponse.getUnits().add(mockedUnit);

        when(soapPortMock.getSubunits()).thenReturn(mockedResponse);

        UnitList result = evasysUserClient.getSubunits();

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

        UserList result = evasysUserClient.getUsersBySubunit(1);

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

        EvasysException exception = assertThrows(EvasysException.class, () -> evasysUserClient.getUsersBySubunit(1));

        assertEquals("No users found in the given subunit", exception.getMessage());
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

        boolean result = evasysUserClient.isTrainerExisting(trainerId, 1);

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

        boolean result = evasysUserClient.isTrainerExisting(trainerId, 1);

        assertFalse(result);
    }

    @Test
    public void shouldCallSoapPortWithCorrectUserWhenUpdatingTrainer() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAINER1ID("1");
        trainingData.setTRAINER1ANREDE("1");
        trainingData.setTRAINER1TITEL("Dr.");
        trainingData.setTRAINER1VNAME("Max");
        trainingData.setTRAINER1NNAME("Mustermann");
        trainingData.setTRAINER1MAIL("max@example.com");

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

        evasysUserClient.updateTrainer(trainingData);

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
        assertEquals(1, captured.getMNAddressId());
    }

    @Test
    public void shouldUpdateAllUsersWithSameExternalIdWhenUpdatingTrainer() throws Exception {
        ZLSOSTEVASYSRFC trainingData = new ZLSOSTEVASYSRFC();
        trainingData.setTRAINER1ID("1");
        trainingData.setTRAINER1ANREDE("1");
        trainingData.setTRAINER1TITEL("Dr.");
        trainingData.setTRAINER1VNAME("Max");
        trainingData.setTRAINER1NNAME("Mustermann");
        trainingData.setTRAINER1MAIL("max@example.com");

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

        evasysUserClient.updateTrainer(trainingData);

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
        trainingData.setTRAINER1ANREDE("1");
        trainingData.setTRAINER1TITEL("Dr.");
        trainingData.setTRAINER1VNAME("Max");
        trainingData.setTRAINER1NNAME("Mustermann");
        trainingData.setTRAINER1MAIL("max@example.com");
        trainingData.setTEILBEREICHID("1");

        evasysUserClient.insertTrainer(trainingData);

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

        evasysUserClient.updateSecondaryTrainer(secondaryTrainer);

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

        evasysUserClient.updateSecondaryTrainer(secondaryTrainer);

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

        evasysUserClient.insertSecondaryTrainer(trainingData, secondaryTrainer);

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
}
