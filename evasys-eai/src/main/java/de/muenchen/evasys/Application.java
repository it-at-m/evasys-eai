package de.muenchen.evasys;

import de.muenchen.evasys.service.SubunitService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import wsdl.soapserver_v100.SoapfaultMessage;
import wsdl.soapserver_v100.UnitList;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner testSoapClient(final SubunitService subunitService) {
        return args -> {
            try {
                final UnitList list = subunitService.getSubunits();
                LOGGER.info("Got {} units!", list.getUnits().size());
            } catch (final SoapfaultMessage e) {
                LOGGER.error("Error while fetching subunits", e);
            }
        };
    }
}
