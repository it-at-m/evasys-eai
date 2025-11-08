package de.muenchen.evasys.soap;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import java.util.List;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.Soapserver;

public final class SoapPortFactory {

    private SoapPortFactory() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static SoapPort createPort(final String endpointUrl, final String username, final String password) {
        final Soapserver service = new Soapserver();
        final SoapPort port = service.getSoapPort();

        final BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);

        final List<Handler> handlers = bp.getBinding().getHandlerChain();
        handlers.add(new SoapHeaderHandler(username, password));
        bp.getBinding().setHandlerChain(handlers);

        return port;
    }
}
