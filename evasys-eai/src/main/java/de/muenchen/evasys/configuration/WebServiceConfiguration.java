package de.muenchen.evasys.configuration;

import de.lhm.pi.evasys.afs.SITrainingASIB;
import de.muenchen.evasys.endpoint.SapServiceEndpoint;
import de.muenchen.evasys.service.TrainingProcessorService;
import jakarta.xml.ws.Endpoint;
import java.util.HashMap;
import java.util.Map;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServiceConfiguration {
    private final SapPoProperties props;

    private final TrainingProcessorService trainingProcessorService;

    public WebServiceConfiguration(final SapPoProperties props, final TrainingProcessorService trainingProcessorService) {
        this.props = props;
        this.trainingProcessorService = trainingProcessorService;
    }

    @Bean
    public SITrainingASIB sapService() {
        return new SapServiceEndpoint(trainingProcessorService);
    }

    @Bean
    public Endpoint endpoint(final Bus bus, final SITrainingASIB sapService) {
        final EndpointImpl endpoint = new EndpointImpl(bus, sapService);
        final Map<String, Object> propsMap = new HashMap<>();
        propsMap.put("ws-security.username", props.username());
        propsMap.put("ws-security.password", props.password());
        endpoint.setProperties(propsMap);
        endpoint.publish("/evasyseai");
        return endpoint;
    }
}
