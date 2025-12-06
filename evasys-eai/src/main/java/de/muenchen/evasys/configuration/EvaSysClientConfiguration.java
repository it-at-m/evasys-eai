package de.muenchen.evasys.configuration;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.muenchen.evasys.soap.SoapHeaderHandler;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.Soapserver;

@Configuration
public class EvaSysClientConfiguration {

    @Bean
    public SoapPort evaSysSoapPort(final EvaSysProperties props) {
        final Soapserver service = new Soapserver();
        final SoapPort port = service.getSoapPort();

        final BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, props.uri());
        bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", 10_000);
        bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", 30_000);

        @SuppressWarnings("rawtypes")
        final List<Handler> handlers = bp.getBinding().getHandlerChain();
        handlers.add(new SoapHeaderHandler(props.username(), props.password()));
        bp.getBinding().setHandlerChain(handlers);

        return port;
    }
}
