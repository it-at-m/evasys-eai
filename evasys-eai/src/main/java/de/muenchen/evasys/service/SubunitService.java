package de.muenchen.evasys.service;

import de.muenchen.evasys.client.SubunitClient;
import org.springframework.stereotype.Service;
import wsdl.soapserver_v100.SoapfaultMessage;
import wsdl.soapserver_v100.UnitList;

@Service
public class SubunitService {

    private final SubunitClient client;

    public SubunitService(SubunitClient client) {
        this.client = client;
    }

    public UnitList getSubunits() throws SoapfaultMessage {
        return client.getSubunits();
    }
}
