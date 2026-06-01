package de.muenchen.evasys.configuration;

import de.muenchen.evasys.client.EvasysTransportLoggingOutInterceptor;
import de.muenchen.evasys.client.SoapHeaderHandler;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import java.util.List;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.Soapserver;

@Configuration
@SuppressWarnings("PMD.CloseResource")
public class EvasysClientConfiguration {

    @Bean
    public SoapPort evasysSoapPort(final EvasysProperties props) {
        final Soapserver service = new Soapserver();
        final SoapPort port = service.getSoapPort();

        final BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, props.uri());

        final Client client = ClientProxy.getClient(port);
        final HTTPConduit conduit = (HTTPConduit) client.getConduit();

        final HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        // evasys/IIS does not reliably handle HTTP chunked transfer encoding.
        // Requests larger than CXF's default chunking threshold may fail with HTML 500
        // or timeout, therefore chunking must stay disabled.
        httpClientPolicy.setAllowChunking(false);
        httpClientPolicy.setConnectionTimeout(props.connectionTimeout().toMillis());
        httpClientPolicy.setReceiveTimeout(props.receiveTimeout().toMillis());

        conduit.setClient(httpClientPolicy);

        client.getOutInterceptors().add(new EvasysTransportLoggingOutInterceptor(httpClientPolicy));

        @SuppressWarnings("rawtypes")
        final List<Handler> handlers = bp.getBinding().getHandlerChain();
        handlers.add(new SoapHeaderHandler(props.username(), props.password()));
        bp.getBinding().setHandlerChain(handlers);

        return port;
    }
}
