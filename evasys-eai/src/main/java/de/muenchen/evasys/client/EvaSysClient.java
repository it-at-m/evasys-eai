package de.muenchen.evasys.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.configuration.EvaSysException;
import de.muenchen.evasys.configuration.EvaSysProperties;
import de.muenchen.evasys.soap.SoapPortFactory;
import jakarta.xml.ws.Holder;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class EvaSysClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaSysClient.class);

    private final SoapPort soapPort;

    @Autowired
    public EvaSysClient(final EvaSysProperties props) {
        this.soapPort = SoapPortFactory.createPort(
                props.uri(),
                props.username(),
                props.password());
    }

    // Test constructor
    protected EvaSysClient(EvaSysProperties props, final SoapPort soapPort) {
        this.soapPort = soapPort;
    }

    public UnitList getSubunits() {
        LOGGER.info("Requesting list of subunits...");
        try {
            return soapPort.getSubunits();
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while requesting subunits", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while requesting subunits", e);
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
            switch (errorCode) {
            case "ERR_305":
                throw new EvaSysException("User not found for subunit", e);
            default:
                throw new EvaSysException("SOAP error code:" + errorCode, e);
            }
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while requesting users by subunit", e);
        }
    }

    public User getUser(final int externalUserId) {
        LOGGER.info("Requesting user data...");
        try {
            final UserIdType userIdType = UserIdType.EXTERNAL;
            final UserList userList = soapPort.getUserByIdConsiderExternalID(String.valueOf(externalUserId), userIdType, false, false, false, false);
            return userList.getUsers().getFirst();
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while requesting user data", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while requesting user data", e);
        }
    }

    public Course getCourse(final int courseId) {
        LOGGER.info("Requesting course data...");
        try {
            final CourseIdType courseIdType = CourseIdType.PUBLIC; // always constant
            return soapPort.getCourse(String.valueOf(courseId), courseIdType, false, false);
        } catch (SoapfaultMessage e) {
            final String errorCode = e.getFaultInfo().getSErrorMessage();
            switch (errorCode) {
            case "ERR_312":
                LOGGER.info("No course found for courseId {}", courseId);
                return null;
            default:
                throw new EvaSysException("SOAP error code:" + errorCode, e);
            }
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while requesting course data", e);
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
            final User updatedUser = new User();
            updatedUser.setMNType(1);
            updatedUser.setMNId(foundUser.getMNId());
            updatedUser.setMSExternalId(trainingData.getTRAINER1ID());
            updatedUser.setMSTitle(trainingData.getTRAINER1TITEL());
            updatedUser.setMSFirstName(trainingData.getTRAINER1VNAME());
            updatedUser.setMSSurName(trainingData.getTRAINER1NNAME());
            updatedUser.setMSEmail(trainingData.getTRAINER1MAIL());
            updatedUser.setMNFbid(Integer.parseInt(trainingData.getTEILBEREICHID()));
            updatedUser.setMNAddressId(Integer.parseInt(trainingData.getTRAINERGESCHL()));
            final Holder<User> userHolder = new Holder<>(updatedUser);
            soapPort.updateUser(userHolder);
            LOGGER.info("Trainer with ID {} sucessfully updated", trainingData.getTRAINER1ID());
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while updating trainer", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while updating trainer", e);
        }
    }

    public void insertTrainer(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Inserting new trainer...");
        try {
            final User newUser = new User();
            newUser.setMNType(1); // always constant (1 = Trainer)
            newUser.setMSExternalId(trainingData.getTRAINER1ID());
            newUser.setMSTitle(trainingData.getTRAINER1TITEL());
            newUser.setMSFirstName(trainingData.getTRAINER1VNAME());
            newUser.setMSSurName(trainingData.getTRAINER1NNAME());
            newUser.setMSEmail(trainingData.getTRAINER1MAIL());
            newUser.setMNFbid(Integer.parseInt(trainingData.getTEILBEREICHID()));
            newUser.setMNAddressId(Integer.parseInt(trainingData.getTRAINERGESCHL()));
            final Holder<User> userHolder = new Holder<>(newUser);
            soapPort.insertUser(userHolder);
            LOGGER.info("Trainer with ID {} sucessfully inserted", trainingData.getTRAINER1ID());
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while inserting trainer", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while inserting trainer", e);
        }
    }

    public boolean hasSecondaryTrainer(final ZLSOSTEVASYSRFC trainingData) {
        return trainingData.getSEKTRAINERID() != null && !trainingData.getSEKTRAINERID().isEmpty();
    }

    public void insertSecondaryTrainer(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Inserting new secondary trainer...");
        try {
            final User newUser = new User();
            newUser.setMNType(1);
            newUser.setMSExternalId(trainingData.getSEKTRAINERID());
            newUser.setMSTitle(trainingData.getSEKTRAINERTITEL());
            newUser.setMSFirstName(trainingData.getSEKTRAINERVNAME());
            newUser.setMSSurName(trainingData.getSEKTRAINERNNAME());
            newUser.setMSEmail(trainingData.getSEKTRAINERMAIL());
            newUser.setMNFbid(Integer.parseInt(trainingData.getTEILBEREICHID()));
            newUser.setMNAddressId(Integer.parseInt(trainingData.getTRAINERGESCHL()));
            final Holder<User> userHolder = new Holder<>(newUser);
            soapPort.insertUser(userHolder);
            LOGGER.info("Secondary trainer with ID {} successfully inserted", trainingData.getSEKTRAINERID());
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while inserting secondary trainer", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while inserting secondary trainer", e);
        }
    }

    public void updateSecondaryTrainer(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Updating secondary trainer data...");
        try {
            final User foundUser = getUser(Integer.parseInt(trainingData.getSEKTRAINERID()));
            final User updatedUser = new User();
            updatedUser.setMNType(1);
            updatedUser.setMNId(foundUser.getMNId());
            updatedUser.setMSExternalId(trainingData.getSEKTRAINERID());
            updatedUser.setMSTitle(trainingData.getSEKTRAINERTITEL());
            updatedUser.setMSFirstName(trainingData.getSEKTRAINERVNAME());
            updatedUser.setMSSurName(trainingData.getSEKTRAINERNNAME());
            updatedUser.setMSEmail(trainingData.getSEKTRAINERMAIL());
            updatedUser.setMNFbid(Integer.parseInt(trainingData.getTEILBEREICHID()));
            updatedUser.setMNAddressId(Integer.parseInt(trainingData.getTRAINERGESCHL()));
            final Holder<User> userHolder = new Holder<>(updatedUser);
            soapPort.updateUser(userHolder);
            LOGGER.info("Secondary trainer with ID {} successfully updated", trainingData.getSEKTRAINERID());
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while updating secondary trainer", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while updating secondary trainer", e);
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
            final Course updatedCourse = new Course();
            final ObjectNode json = buildCourseJson(trainingData);

            updatedCourse.setMNCourseId(foundCourse.getMNCourseId());
            updatedCourse.setMNCourseType(Integer.parseInt(trainingData.getTRAININGART()));
            updatedCourse.setMSPubCourseId(trainingData.getTRAININGID());
            updatedCourse.setMSCustomFieldsJSON(json.toString());
            updatedCourse.setMNUserId(foundUser.getMNId());
            updatedCourse.setMNFbid(Integer.parseInt(trainingData.getTEILBEREICHID()));

            final Holder<Course> courseHolder = new Holder<>(updatedCourse);
            soapPort.updateCourse(courseHolder, false);
            LOGGER.info("Course with ID {} successfully updated", trainingData.getTRAININGID());
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while updating course", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while updating course", e);
        }
    }

    public void insertCourse(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Inserting new course...");
        try {
            final User foundUser = getUser(Integer.parseInt(trainingData.getTRAINER1ID()));
            final Course newCourse = new Course();
            final ObjectNode json = buildCourseJson(trainingData);

            newCourse.setMNCourseId(Integer.parseInt(trainingData.getTRAININGID()));
            newCourse.setMSProgramOfStudy(trainingData.getTRAININGSTYPKUERZEL());
            newCourse.setMSCourseTitle(trainingData.getTRAININGTITEL());
            newCourse.setMSRoom(trainingData.getTRAININGRAUM());
            newCourse.setMNCourseType(Integer.parseInt(trainingData.getTRAININGART()));
            newCourse.setMSPubCourseId(trainingData.getTRAININGID());
            newCourse.setMNCountStud(Integer.parseInt(trainingData.getTRAININGTNANZAHL()));
            newCourse.setMSCustomFieldsJSON(json.toString());
            newCourse.setMNUserId(foundUser.getMNId());
            newCourse.setMNFbid(Integer.parseInt(trainingData.getTEILBEREICHID()));

            soapPort.insertCourse(newCourse);
            LOGGER.info("Course with ID {} sucessfully inserted", trainingData.getTRAININGID());
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while inserting course", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while inserting course", e);
        }
    }

    private ObjectNode buildCourseJson(final ZLSOSTEVASYSRFC trainingData) {
        final ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("1", trainingData.getTRAINERGESCHL());
        json.put("2", trainingData.getTRAINEROBJTYP());
        json.put("3", trainingData.getFIRMA());
        json.put("4", ""); // always empty
        json.put("5", ""); // always empty
        json.put("6", trainingData.getTEILBEREICHID());
        json.put("7", trainingData.getTRAININGBEGINN());
        json.put("8", trainingData.getTRAININGENDE());
        json.put("9", String.join(" ", trainingData.getVAVNAME(), trainingData.getVANNAME()));
        json.put("10", String.join(" ", trainingData.getSBVNAME(), trainingData.getSBNNAME()));
        json.put("11", trainingData.getTRAININGDAUERTAGE());
        json.put("12", trainingData.getTRAININGDAUERSTD());
        return json;
    }
}
