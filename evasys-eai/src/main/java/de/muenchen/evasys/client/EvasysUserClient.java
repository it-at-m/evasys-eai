package de.muenchen.evasys.client;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvasysException;
import de.muenchen.evasys.mapper.SapEvasysMapper;
import de.muenchen.evasys.model.SecondaryTrainer;
import jakarta.xml.ws.Holder;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.UnitList;
import wsdl.soapserver_v100.User;
import wsdl.soapserver_v100.UserIdType;
import wsdl.soapserver_v100.UserList;

@Component
public class EvasysUserClient extends AbstractEvasysClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvasysUserClient.class);
    private static final String ERR_USER_NOT_FOUND = "ERR_302";
    private static final String ERR_NO_USERS_FOUND = "ERR_305";

    private final SapEvasysMapper mapper;

    public EvasysUserClient(
            final SoapPort soapPort,
            final SoapExecutor soapExecutor,
            final SapEvasysMapper mapper) {
        super(soapPort, soapExecutor);
        this.mapper = mapper;
    }

    @Override
    protected String clientName() {
        return "evasys-user-client";
    }

    /* ------------------------- READ OPERATIONS ------------------------- */

    public UnitList getSubunits() {
        LOGGER.info("Requesting list of subunits...");
        return soapExecutor.execute(
                "requesting subunits",
                soapPort::getSubunits);
    }

    public UserList getUsersBySubunit(final int subunitId) {
        LOGGER.info("Requesting list of users by subunit...");
        try {
            final UserList users = soapExecutor.execute(
                    "requesting users by subunit",
                    () -> soapPort.getUsersBySubunit(
                            subunitId,
                            false, false, false, false));
            LOGGER.info("Received {} users", users.getUsers().size());
            return users;
        } catch (EvasysException e) {
            if (ERR_NO_USERS_FOUND.equals(extractErrorCode(e))) {
                throw new EvasysException("No users found in the given subunit", e);
            }
            throw e;
        }
    }

    public UserList getUsersByExternalId(final String externalUserId) {
        LOGGER.info("Requesting all users with external ID {}...", externalUserId);
        try {
            final UserList userList = soapExecutor.execute(
                    "requesting users by external ID",
                    () -> soapPort.getUserByIdConsiderExternalID(
                            externalUserId,
                            UserIdType.EXTERNAL,
                            false, false, false, false));
            LOGGER.info("Found {} users with external ID {}", userList.getUsers().size(), externalUserId);
            return userList;
        } catch (EvasysException e) {
            if (ERR_USER_NOT_FOUND.equals(extractErrorCode(e))) {
                throw new EvasysException(
                        "No user found for id " + externalUserId, e);
            }
            throw e;
        }
    }

    public User getUserByExternalIdAndSubunit(
            final String externalUserId,
            final String teilbereichId) {
        LOGGER.info("Requesting user with external ID {} and subunit ID {}...", externalUserId, teilbereichId);
        final int subunitId = parseSubunitId(teilbereichId);
        final UserList userList = getUsersByExternalId(externalUserId);

        final User matchingUser = userList.getUsers()
                .stream()
                .filter(u -> u.getMNFbid() != null && u.getMNFbid() == subunitId)
                .findFirst()
                .orElseThrow(() -> new EvasysException(
                        "No user found for external ID "
                                + externalUserId
                                + " and subunit ID "
                                + teilbereichId));

        LOGGER.info("Found matching user with ID {} and subunit ID {}",
                matchingUser.getMNId(), matchingUser.getMNFbid());
        return matchingUser;
    }

    /* ------------------------- EXISTENCE CHECKS ------------------------- */

    public boolean isTrainerExisting(final String trainerId, final int subunitId) {
        LOGGER.info("Checking whether trainer exists...");
        try {
            return getUsersBySubunit(subunitId)
                    .getUsers()
                    .stream()
                    .anyMatch(u -> trainerId.equals(u.getMSExternalId()));
        } catch (Exception e) {
            LOGGER.warn("Trainer existence check failed: {}", e.getMessage());
            return false;
        }
    }

    /* ------------------------- TRAINER HANDLING ------------------------- */

    public void insertTrainer(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Inserting new trainer...");
        soapExecutor.executeVoid(
                "inserting trainer",
                () -> soapPort.insertUser(
                        new Holder<>(mapper.mapToTrainer(trainingData))));
        LOGGER.info("Trainer with ID {} successfully inserted", trainingData.getTRAINER1ID());
    }

    public void updateTrainer(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Updating trainer data...");
        updateUsers(
                trainingData.getTRAINER1ID(),
                user -> applyPrimaryTrainerUpdates(user, trainingData));
        LOGGER.info("Successfully updated user(s) with external ID {}", trainingData.getTRAINER1ID());
    }

    public void insertSecondaryTrainer(
            final ZLSOSTEVASYSRFC trainingData,
            final SecondaryTrainer secondaryTrainer) {
        LOGGER.info("Inserting new secondary trainer...");
        soapExecutor.executeVoid(
                "inserting secondary trainer",
                () -> soapPort.insertUser(
                        new Holder<>(mapper.mapToSecondaryTrainer(
                                secondaryTrainer, trainingData))));
        LOGGER.info("Secondary trainer with ID {} successfully inserted", secondaryTrainer.id());
    }

    public void updateSecondaryTrainer(final SecondaryTrainer secondaryTrainer) {
        LOGGER.info("Updating secondary trainer data...");
        updateUsers(
                secondaryTrainer.id(),
                user -> applySecondaryTrainerUpdates(user, secondaryTrainer));
        LOGGER.info("Successfully updated user(s) with external ID {}", secondaryTrainer.id());
    }

    /* ------------------------- INTERNAL HELPERS ------------------------- */

    private void updateUsers(final String externalId, final Consumer<User> updater) {
        final List<User> users = getUsersByExternalId(externalId).getUsers();
        LOGGER.info("Updating {} user(s) with external ID {}", users.size(), externalId);

        soapExecutor.executeVoid(
                "updating users",
                () -> {
                    for (final User user : users) {
                        updater.accept(user);
                        soapPort.updateUser(new Holder<>(user));
                    }
                });
    }

    private void applyPrimaryTrainerUpdates(
            final User user,
            final ZLSOSTEVASYSRFC data) {
        updateIfNotEmpty(data.getTRAINER1ID(), user::setMSExternalId);
        updateIfNotEmptyInt(data.getTRAINERGESCHL(), user::setMNAddressId);
        updateIfNotEmpty(data.getTRAINER1TITEL(), user::setMSTitle);
        updateIfNotEmpty(data.getTRAINER1VNAME(), user::setMSFirstName);
        updateIfNotEmpty(data.getTRAINER1NNAME(), user::setMSSurName);
        updateIfNotEmpty(data.getTRAINER1MAIL(), user::setMSEmail);
        updateIfNotEmptyInt(data.getTEILBEREICHID(), user::setMNFbid);
    }

    private void applySecondaryTrainerUpdates(
            final User user,
            final SecondaryTrainer trainer) {
        updateIfNotEmpty(trainer.id(), user::setMSExternalId);
        updateIfNotEmptyInt(trainer.anrede(), user::setMNAddressId);
        updateIfNotEmpty(trainer.titel(), user::setMSTitle);
        updateIfNotEmpty(trainer.vorname(), user::setMSFirstName);
        updateIfNotEmpty(trainer.nachname(), user::setMSSurName);
        updateIfNotEmpty(trainer.email(), user::setMSEmail);
    }

    private void updateIfNotEmpty(final String value, final Consumer<String> setter) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }

    private void updateIfNotEmptyInt(final String value, final IntConsumer setter) {
        if (value != null && !value.isBlank()) {
            try {
                setter.accept(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid integer value '{}'", value);
            }
        }
    }

    private int parseSubunitId(final String teilbereichId) {
        if (teilbereichId == null || teilbereichId.isBlank()) {
            throw new EvasysException("TEILBEREICHID must not be empty");
        }
        try {
            return Integer.parseInt(teilbereichId);
        } catch (NumberFormatException e) {
            throw new EvasysException(
                    "Invalid TEILBEREICHID: " + teilbereichId, e);
        }
    }
}
