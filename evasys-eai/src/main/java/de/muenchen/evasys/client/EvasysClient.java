package de.muenchen.evasys.client;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvasysException;
import de.muenchen.evasys.mapper.SapEvasysMapper;
import de.muenchen.evasys.model.SecondaryTrainer;
import jakarta.xml.ws.Holder;
import java.util.List;
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
    private static final String ERR_USER_NOT_FOUND = "ERR_305";
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
            if (ERR_USER_NOT_FOUND.equals(errorCode)) {
                throw new EvasysException("User not found for subunit", e);
            }
            throw new EvasysException("SOAP error code:" + errorCode, e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while requesting users by subunit", e);
        }
    }

    public User getUser(final int externalUserId) {
        LOGGER.info("Requesting user data...");
        try {
            final UserIdType userIdType = UserIdType.EXTERNAL;
            final UserList userList = soapPort.getUserByIdConsiderExternalID(String.valueOf(externalUserId), userIdType, false, false, false, false);
            return userList.getUsers().getFirst();
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while requesting user data", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while requesting user data", e);
        }
    }

    public Course getCourse(final int courseId) {
        LOGGER.info("Requesting course data...");
        try {
            final CourseIdType courseIdType = CourseIdType.PUBLIC; // always constant
            return soapPort.getCourse(String.valueOf(courseId), courseIdType, false, false);
        } catch (SoapfaultMessage e) {
            final String errorCode = e.getFaultInfo().getSErrorMessage();
            if (ERR_COURSE_NOT_FOUND.equals(errorCode)) {
                LOGGER.info("No course found for courseId {}", courseId);
                return null;
            }
            throw new EvasysException("SOAP error code:" + errorCode, e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while requesting course data", e);
        }
    }

    public boolean isTrainerExisting(final int trainerId, final int subunitId) {
        LOGGER.info("Checking whether trainer exists...");
        try {
            final UserList users = getUsersBySubunit(subunitId);
            final List<User> userList = users.getUsers();
            return userList.stream().anyMatch(user -> String.valueOf(trainerId).equals(user.getMSExternalId()));
        } catch (Exception e) {
            LOGGER.error("Error", e);
            return false;
        }
    }

    public void updateTrainer(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Updating trainer data...");
        try {
            final User foundUser = getUser(Integer.parseInt(trainingData.getTRAINER1ID()));
            final User updatedTrainer = mapper.mapToTrainer(trainingData);
            updatedTrainer.setMNId(foundUser.getMNId());
            final Holder<User> userHolder = new Holder<>(updatedTrainer);
            soapPort.updateUser(userHolder);
            LOGGER.info("Trainer with ID {} successfully updated", trainingData.getTRAINER1ID());
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while updating trainer", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while updating trainer", e);
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

    public void updateSecondaryTrainer(final ZLSOSTEVASYSRFC trainingData, final SecondaryTrainer secondaryTrainer) {
        LOGGER.info("Updating secondary trainer data...");
        try {
            final User foundUser = getUser(Integer.parseInt(secondaryTrainer.id()));
            final User updatedUser = mapper.mapToSecondaryTrainer(secondaryTrainer, trainingData);
            updatedUser.setMNId(foundUser.getMNId());
            final Holder<User> userHolder = new Holder<>(updatedUser);
            soapPort.updateUser(userHolder);
            LOGGER.info("Secondary trainer with ID {} successfully updated", secondaryTrainer.id());
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
            final User foundUser = getUser(Integer.parseInt(trainingData.getTRAINER1ID()));
            final Course foundCourse = getCourse(Integer.parseInt(trainingData.getTRAININGID()));
            final Course updatedCourse = mapper.mapToCourse(trainingData);

            updatedCourse.setMNCourseId(foundCourse.getMNCourseId());
            updatedCourse.setMNUserId(foundUser.getMNId());

            final Holder<Course> courseHolder = new Holder<>(updatedCourse);
            soapPort.updateCourse(courseHolder, false);
            LOGGER.info("Course with ID {} successfully updated", trainingData.getTRAININGID());
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while updating course", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while updating course", e);
        }
    }

    public void insertCourse(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Inserting new course...");
        try {
            final User foundUser = getUser(Integer.parseInt(trainingData.getTRAINER1ID()));
            final Course newCourse = mapper.mapToCourse(trainingData);

            newCourse.setMNUserId(foundUser.getMNId());

            soapPort.insertCourse(newCourse);
            LOGGER.info("Course with ID {} successfully inserted", trainingData.getTRAININGID());
        } catch (SoapfaultMessage e) {
            throw new EvasysException("SOAP error while inserting course", e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while inserting course", e);
        }
    }
}
