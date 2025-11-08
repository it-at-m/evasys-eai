package de.muenchen.evasys.client;

import de.muenchen.evasys.configuration.SoapProperties;
import de.muenchen.evasys.soap.SoapPortFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.SoapfaultMessage;
import wsdl.soapserver_v100.UnitList;

@Component
public class SubunitClient {

    private static final Logger log = LoggerFactory.getLogger(SubunitClient.class);
    private final SoapPort soapPort;

    public SubunitClient(SoapProperties props) {
        this.soapPort = SoapPortFactory.createPort(
                props.endpointUrl(),
                props.username(),
                props.password());
        log.info("SOAP client configured to send requests to: {}", props.endpointUrl());
    }

    public UnitList getSubunits() throws SoapfaultMessage {
        log.info("Requesting list of subunits...");
        UnitList result = soapPort.getSubunits();
        log.info("Received {} units", result.getUnits().size());
        return result;
    }
}
