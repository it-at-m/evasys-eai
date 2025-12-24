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
import wsdl.soapserver_v100.Course;
import wsdl.soapserver_v100.CourseIdType;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.SoapfaultMessage;
import wsdl.soapserver_v100.UnitList;
import wsdl.soapserver_v100.User;
import wsdl.soapserver_v100.UserIdType;
import wsdl.soapserver_v100.UserList;

@Component
public class EvasysClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvasysClient.class);
    private static final String ERR_USER_NOT_FOUND = "ERR_302";
    private static final String ERR_NO_USERS_FOUND = "ERR_305";
    private static final String ERR_COURSE_NOT_FOUND = "ERR_312";

    private final SoapPort soapPort;
    private final SapEvasysMapper mapper;

    public EvasysClient(final SoapPort soapPort, final SapEvasysMapper mapper) {
        this.soapPort = soapPort;
        this.mapper = mapper;
    }

    public UnitList getSubunits() {
        LOGGER.info("Requesting list of subunits...");
        try {
            return soapPort.getSubunits();
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while requesting subunits", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while requesting subunits", e);
        }
    }

    public UserList getUsersBySubunit(final int subunitId) {
        LOGGER.info("Requesting list of users by subunit...");
        try {
            final UserList users = soapPort.getUsersBySubunit(subunitId, false, false, false, false);
            LOGGER.info("Received {} users", users.getUsers().size());
            return users;
        } catch (SoapfaultMessage e) {
            final String errorCode = e.getFaultInfo().getSErrorMessage();
            if (ERR_NO_USERS_FOUND.equals(errorCode)) {
                throw new EvasysException("No users found in the given subunit", e);
            }
            throw new EvasysException("SOAP error code:" + errorCode, e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while requesting users by subunit", e);
        }
    }

    private UserList getAllUsers(final String externalUserId) {
        LOGGER.info("Requesting all users with external ID {}...", externalUserId);
        try {
            final UserIdType userIdType = UserIdType.EXTERNAL;
            final UserList userList = soapPort.getUserByIdConsiderExternalID(externalUserId, userIdType, false, false, false, false);
            LOGGER.info("Found {} users with external ID {}", userList.getUsers().size(), externalUserId);
            return userList;
        } catch (SoapfaultMessage e) {
            final String errorCode = e.getFaultInfo().getSErrorMessage();
            if (ERR_USER_NOT_FOUND.equals(errorCode)) {
                throw new EvasysException("No user found for the given id " + externalUserId, e);
            }
            throw new EvasysException("SOAP error code: " + errorCode, e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while requesting user data", e);
        }
    }

    private User getUserByExternalIdAndSubunit(final String externalUserId, final String teilbereichId) {
        LOGGER.info("Requesting user with external ID {} and subunit ID {}...", externalUserId, teilbereichId);

        validateTeilbereichId(teilbereichId);
        final int subunitId = parseSubunitId(teilbereichId);
        final List<User> users = getAllUsers(externalUserId).getUsers();

        LOGGER.info("Found {} user(s) with external ID {}, filtering by subunit ID {}",
                users.size(), externalUserId, subunitId);

        final User matchingUser = findUserBySubunitId(users, externalUserId, subunitId, teilbereichId);

        LOGGER.info("Found matching user with ID {} and subunit ID {}",
                matchingUser.getMNId(), matchingUser.getMNFbid());
        return matchingUser;
    }

    private void validateTeilbereichId(final String teilbereichId) {
        if (teilbereichId == null || teilbereichId.isBlank()) {
            throw new EvasysException("Subunit ID (TEILBEREICHID) is required to find the correct user");
        }
    }

    private int parseSubunitId(final String teilbereichId) {
        try {
            return Integer.parseInt(teilbereichId);
        } catch (NumberFormatException e) {
            throw new EvasysException("Invalid subunit ID format: " + teilbereichId, e);
        }
    }

    private User findUserBySubunitId(final List<User> users, final String externalUserId,
                                     final int subunitId, final String teilbereichId) {
        return users.stream()
                .filter(user -> user.getMNFbid() != null && user.getMNFbid() == subunitId)
                .findFirst()
                .orElseThrow(() -> new EvasysException(
                        String.format("No user found with external ID %s and subunit ID %s",
                                externalUserId, teilbereichId)));
    }

    public Course getCourse(final int courseId) {
        LOGGER.info("Requesting course data...");
        try {
            final CourseIdType courseIdType = CourseIdType.PUBLIC; // always constant
            return soapPort.getCourse(String.valueOf(courseId), courseIdType, false, false);
        } catch (SoapfaultMessage e) {
            final String errorCode = e.getFaultInfo().getSErrorMessage();
            if (ERR_COURSE_NOT_FOUND.equals(errorCode)) {
                throw new EvasysException("No course found for the given id " + courseId, e);
            }
            throw new EvasysException("SOAP error code:" + errorCode, e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while requesting course data", e);
        }
    }

    public boolean isTrainerExisting(final String trainerId, final int subunitId) {
        LOGGER.info("Checking whether trainer exists...");
        try {
            final UserList users = getUsersBySubunit(subunitId);
            final List<User> userList = users.getUsers();
            return userList.stream().anyMatch(user -> trainerId.equals(user.getMSExternalId()));
        } catch (Exception e) {
            LOGGER.error("Error", e);
            return false;
        }
    }

    public void updateTrainer(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Updating trainer data...");
        try {
            final UserList userList = getAllUsers(trainingData.getTRAINER1ID());
            final List<User> users = userList.getUsers();

            LOGGER.info("Updating {} user(s) with external ID {}", users.size(), trainingData.getTRAINER1ID());

            for (final User user : users) {
                updateIfNotEmpty(trainingData.getTRAINER1ID(), user::setMSExternalId);
                updateIfNotEmptyInt(trainingData.getTRAINERGESCHL(), user::setMNAddressId);
                updateIfNotEmpty(trainingData.getTRAINER1TITEL(), user::setMSTitle);
                updateIfNotEmpty(trainingData.getTRAINER1VNAME(), user::setMSFirstName);
                updateIfNotEmpty(trainingData.getTRAINER1NNAME(), user::setMSSurName);
                updateIfNotEmpty(trainingData.getTRAINER1MAIL(), user::setMSEmail);
                updateIfNotEmptyInt(trainingData.getTEILBEREICHID(), user::setMNFbid);

                final Holder<User> userHolder = new Holder<>(user);
                soapPort.updateUser(userHolder);
            }

            LOGGER.info("Successfully updated {} user(s) with external ID {}", users.size(), trainingData.getTRAINER1ID());
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while updating trainer", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while updating trainer", e);
        }
    }

    public void updateIfNotEmpty(final String newValue, final Consumer<String> setter) {
        if (newValue != null && !newValue.isBlank()) {
            setter.accept(newValue);
        }
    }

    private void updateIfNotEmptyInt(final String value, final IntConsumer setter) {
        if (value != null && !value.isBlank()) {
            try {
                setter.accept(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                LOGGER.warn("Cannot parse integer from '{}'", value);
            }
        }
    }

    public void insertTrainer(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Inserting new trainer...");
        try {
            final User newTrainer = mapper.mapToTrainer(trainingData);
            final Holder<User> userHolder = new Holder<>(newTrainer);
            soapPort.insertUser(userHolder);
            LOGGER.info("Trainer with ID {} successfully inserted", trainingData.getTRAINER1ID());
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while inserting trainer", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while inserting trainer", e);
        }
    }

    public void insertSecondaryTrainer(final ZLSOSTEVASYSRFC trainingData, final SecondaryTrainer secondaryTrainer) {
        LOGGER.info("Inserting new secondary trainer...");
        try {
            final User newUser = mapper.mapToSecondaryTrainer(secondaryTrainer, trainingData);
            final Holder<User> userHolder = new Holder<>(newUser);
            soapPort.insertUser(userHolder);
            LOGGER.info("Secondary trainer with ID {} successfully inserted", secondaryTrainer.id());
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while inserting secondary trainer", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while inserting secondary trainer", e);
        }
    }

    public void updateSecondaryTrainer(final SecondaryTrainer secondaryTrainer) {
        LOGGER.info("Updating secondary trainer data...");
        try {
            final UserList userList = getAllUsers(secondaryTrainer.id());
            final List<User> users = userList.getUsers();

            LOGGER.info("Updating {} user(s) with external ID {}", users.size(), secondaryTrainer.id());

            for (final User user : users) {
                updateIfNotEmpty(secondaryTrainer.id(), user::setMSExternalId);
                updateIfNotEmptyInt(secondaryTrainer.anrede(), user::setMNAddressId);
                updateIfNotEmpty(secondaryTrainer.titel(), user::setMSTitle);
                updateIfNotEmpty(secondaryTrainer.vorname(), user::setMSFirstName);
                updateIfNotEmpty(secondaryTrainer.nachname(), user::setMSSurName);
                updateIfNotEmpty(secondaryTrainer.email(), user::setMSEmail);

                final Holder<User> userHolder = new Holder<>(user);
                soapPort.updateUser(userHolder);
            }

            LOGGER.info("Successfully updated {} user(s) with external ID {}", users.size(), secondaryTrainer.id());
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while updating secondary trainer", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while updating secondary trainer", e);
        }
    }

    public boolean isCourseExisting(final int courseId) {
        LOGGER.info("Checking whether course exists...");
        try {
            final Course foundCourse = getCourse(courseId);
            return foundCourse != null;
        } catch (Exception e) {
            LOGGER.error("Error", e);
            return false;
        }
    }

    public void updateCourse(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Updating course data...");
        try {
            final User foundUser = getUserByExternalIdAndSubunit(
                    trainingData.getTRAINER1ID(),
                    trainingData.getTEILBEREICHID());
            final Course foundCourse = getCourse(Integer.parseInt(trainingData.getTRAININGID()));
            final Course updatedCourse = mapper.mapToCourse(trainingData);

            updatedCourse.setMNCourseId(foundCourse.getMNCourseId());
            updatedCourse.setMNUserId(foundUser.getMNId());

            final Holder<Course> courseHolder = new Holder<>(updatedCourse);
            soapPort.updateCourse(courseHolder, false);
            LOGGER.info("Course with ID {} successfully updated", trainingData.getTRAININGID());
        } catch (EvasysException e) {
            throw e;
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while updating course", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while updating course", e);
        }
    }

    public void insertCourse(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Inserting new course...");
        try {
            final User foundUser = getUserByExternalIdAndSubunit(
                    trainingData.getTRAINER1ID(),
                    trainingData.getTEILBEREICHID());
            final Course newCourse = mapper.mapToCourse(trainingData);

            newCourse.setMNUserId(foundUser.getMNId());

            soapPort.insertCourse(newCourse);
            LOGGER.info("Course with ID {} successfully inserted", trainingData.getTRAININGID());
        } catch (EvasysException e) {
            throw e;
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while inserting course", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while inserting course", e);
        }
    }
}
