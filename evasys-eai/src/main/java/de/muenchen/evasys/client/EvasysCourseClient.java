package de.muenchen.evasys.client;

import com.sap.document.sap.rfc.functions.ZLSOSTEVASYSRFC;
import de.muenchen.evasys.exception.EvasysException;
import de.muenchen.evasys.mapper.SapEvasysMapper;
import jakarta.xml.ws.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import wsdl.soapserver_v100.Course;
import wsdl.soapserver_v100.CourseIdType;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.User;

@Component
public class EvasysCourseClient extends AbstractEvasysClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvasysCourseClient.class);
    private static final String ERR_COURSE_NOT_FOUND = "ERR_312";

    private final SapEvasysMapper mapper;
    private final EvasysUserClient userClient;

    public EvasysCourseClient(
            final SoapPort soapPort,
            final SoapExecutor soapExecutor,
            final SapEvasysMapper mapper,
            final EvasysUserClient userClient) {
        super(soapPort, soapExecutor);
        this.mapper = mapper;
        this.userClient = userClient;
    }

    @Override
    protected String clientName() {
        return "evasys-course-client";
    }

    /* ------------------------- READ OPERATIONS ------------------------- */

    public Course getCourse(final int courseId) {
        LOGGER.info("Requesting course {}", courseId);
        try {
            return soapExecutor.execute(
                    "requesting course",
                    () -> soapPort.getCourse(
                            String.valueOf(courseId),
                            CourseIdType.PUBLIC,
                            false, false));
        } catch (EvasysException e) {
            if (ERR_COURSE_NOT_FOUND.equals(extractErrorCode(e))) {
                throw new EvasysException("No course found for the given id " + courseId, e);
            }
            throw e;
        }
    }

    /* ------------------------- EXISTENCE CHECKS ------------------------- */

    public boolean isCourseExisting(final int courseId) {
        try {
            return getCourse(courseId) != null;
        } catch (Exception e) {
            LOGGER.warn("Course existence check failed: {}", e.getMessage());
            return false;
        }
    }

    /* ------------------------- COURSE HANDLING ------------------------- */

    public void insertCourse(final ZLSOSTEVASYSRFC trainingData) {
        final User trainer = userClient.getUserByExternalIdAndSubunit(
                trainingData.getTRAINER1ID(),
                trainingData.getTEILBEREICHID());

        final Course course = mapper.mapToCourse(trainingData);
        course.setMNUserId(trainer.getMNId());

        soapExecutor.executeVoid(
                "inserting course",
                () -> soapPort.insertCourse(course));
    }

    public void updateCourse(final ZLSOSTEVASYSRFC trainingData) {
        final User trainer = userClient.getUserByExternalIdAndSubunit(
                trainingData.getTRAINER1ID(),
                trainingData.getTEILBEREICHID());

        final Course existing = getCourse(
                Integer.parseInt(trainingData.getTRAININGID()));

        final Course updated = mapper.mapToCourse(trainingData);
        updated.setMNCourseId(existing.getMNCourseId());
        updated.setMNUserId(trainer.getMNId());

        soapExecutor.executeVoid(
                "updating course",
                () -> soapPort.updateCourse(new Holder<>(updated), false));
    }
}
