package de.muenchen.evasys.soap;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import java.util.List;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.Soapserver;

public class SoapPortFactory {

    public static SoapPort createPort(String endpointUrl, String username, String password) {
        Soapserver service = new Soapserver();
        SoapPort port = service.getSoapPort();

        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);

        List<Handler> handlers = bp.getBinding().getHandlerChain();
        handlers.add(new SoapHeaderHandler(username, password));
        bp.getBinding().setHandlerChain(handlers);

        return port;
    }
}
