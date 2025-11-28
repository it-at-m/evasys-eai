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
            throw new EvaSysException("SOAP error while requesting users by subunit", e);
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
            throw new EvaSysException("SOAP error while requesting course data", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while requesting course data", e);
        }
    }

    public boolean isTrainerExisting(final int trainerId, final int subunitId) {
        try {
            final UserList users = getUsersBySubunit(subunitId);
            final List<User> userList = users.getUsers();
            return userList.stream().anyMatch(user -> user.getMNId() == trainerId);
        } catch (Exception e) {
            LOGGER.error("Error", e);
            return false;
        }
    }

    public void updateTrainer(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Updating trainer data...");
        try {
            final User updatedUser = new User();
            updatedUser.setMNType(1);
            updatedUser.setMNId(Integer.parseInt(trainingData.getTRAINER1ID()));
            updatedUser.setMSExternalId(trainingData.getTRAINER1ID());
            updatedUser.setMSTitle(trainingData.getTRAINER1TITEL());
            updatedUser.setMSFirstName(trainingData.getTRAINER1VNAME());
            updatedUser.setMSSurName(trainingData.getTRAINER1NNAME());
            updatedUser.setMSEmail(trainingData.getTRAINER1MAIL());
            updatedUser.setMNFbid(Integer.parseInt(trainingData.getTEILBEREICHID()));
            updatedUser.setMNAddressId(Integer.parseInt(trainingData.getTRAINERGESCHL()));
            final Holder<User> userHolder = new Holder<>(updatedUser);
            soapPort.updateUser(userHolder);
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
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while inserting trainer", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while inserting trainer", e);
        }
    }

    public boolean isCourseExisting(final int courseId) {
        try {
            final CourseIdType courseIdType = CourseIdType.PUBLIC; // always constant
            final Course foundCourse = soapPort.getCourse(String.valueOf(courseId), courseIdType, false, false);
            return foundCourse.getMNCourseId() == courseId;
        } catch (Exception e) {
            LOGGER.error("Error", e);
            return false;
        }
    }

    public void updateCourse(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Updating course data...");
        try {
            final Course updatedCourse = new Course();
            final ObjectNode json = buildCourseJson(trainingData);

            updatedCourse.setMNCourseId(Integer.parseInt(trainingData.getTRAININGID()));
            updatedCourse.setMNCourseType(1); // always constant (1 = Standard)
            updatedCourse.setMSPubCourseId(trainingData.getTRAININGID());
            updatedCourse.setMSCustomFieldsJSON(json.toString());
            updatedCourse.setMNUserId(Integer.parseInt(trainingData.getTRAINER1ID()));
            updatedCourse.setMNFbid(Integer.parseInt(trainingData.getTEILBEREICHID()));

            final Holder<Course> courseHolder = new Holder<>(updatedCourse);
            soapPort.updateCourse(courseHolder, false);
        } catch (SoapfaultMessage e) {
            throw new EvaSysException("SOAP error while updating course", e);
        } catch (Exception e) {
            throw new EvaSysException("Unexpected error while updating course", e);
        }
    }

    public void insertCourse(final ZLSOSTEVASYSRFC trainingData) {
        LOGGER.info("Inserting new course...");
        try {
            final Course newCourse = new Course();
            final ObjectNode json = buildCourseJson(trainingData);

            newCourse.setMNCourseId(Integer.parseInt(trainingData.getTRAININGID()));
            newCourse.setMSProgramOfStudy(trainingData.getTRAININGSTYPKUERZEL());
            newCourse.setMSCourseTitle(trainingData.getTRAININGTITEL());
            newCourse.setMSRoom(trainingData.getTRAININGRAUM());
            newCourse.setMNCourseType(1); // always constant (1 = Standard)
            newCourse.setMSPubCourseId(trainingData.getTRAININGID());
            newCourse.setMNCountStud(Integer.parseInt(trainingData.getTRAININGTNANZAHL()));
            newCourse.setMSCustomFieldsJSON(json.toString());
            newCourse.setMNUserId(Integer.parseInt(trainingData.getTRAINER1ID()));
            newCourse.setMNFbid(Integer.parseInt(trainingData.getTEILBEREICHID()));

            soapPort.insertCourse(newCourse);
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
