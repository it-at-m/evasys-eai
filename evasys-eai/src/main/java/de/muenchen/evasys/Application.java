package de.muenchen.evasys;

import de.muenchen.evasys.service.SubunitService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import wsdl.soapserver_v100.SoapfaultMessage;
import wsdl.soapserver_v100.UnitList;

@SpringBootApplication
@ConfigurationPropertiesScan
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner testSoapClient(SubunitService subunitService) {
        return args -> {
            try {
                UnitList list = subunitService.getSubunits();
                System.out.println("Got " + list.getUnits().size() + " units!");
            } catch (SoapfaultMessage e) {
                e.printStackTrace();
            }
        };
    }
}
