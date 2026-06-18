package de.muenchen.evasys.client;

import de.muenchen.evasys.exception.EvasysException;
import org.springframework.stereotype.Component;
import wsdl.soapserver_v100.SoapfaultMessage;
import wsdl.soapserver_v100.TSoapfault;

@Component
public class SoapExecutor {

    @FunctionalInterface
    public interface SoapCall<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    public interface SoapRunnable {
        void run() throws Exception;
    }

    public <T> T execute(final String action, final SoapCall<T> call) {
        try {
            return call.call();
        } catch (SoapfaultMessage e) {
            throw mapSoapFault(action, e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while " + action, e);
        }
    }

    public void executeVoid(final String action, final SoapRunnable runnable) {
        try {
            runnable.run();
        } catch (SoapfaultMessage e) {
            throw mapSoapFault(action, e);
        } catch (Exception e) {
            throw new EvasysException("Unexpected error while " + action, e);
        }
    }

    private EvasysException mapSoapFault(final String action, final SoapfaultMessage e) {
        final TSoapfault faultInfo = e.getFaultInfo();

        final String errorCode = faultInfo != null
                ? faultInfo.getSErrorMessage()
                : "UNKNOWN";

        final String errorDetails = faultInfo != null
                ? faultInfo.getSDetails()
                : "UNKNOWN";

        return new EvasysException(
                String.format(
                        "SOAP error while %s (code=%s, details=%s)",
                        action, errorCode, errorDetails),
                e);
    }
}
