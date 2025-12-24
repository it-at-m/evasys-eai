package de.muenchen.evasys.client;

import de.muenchen.evasys.exception.EvasysException;
import wsdl.soapserver_v100.SoapPort;
import wsdl.soapserver_v100.SoapfaultMessage;

abstract class AbstractEvasysClient {

    protected final SoapPort soapPort;
    protected final SoapExecutor soapExecutor;

    protected AbstractEvasysClient(
            final SoapPort soapPort,
            final SoapExecutor soapExecutor) {
        this.soapPort = soapPort;
        this.soapExecutor = soapExecutor;
    }

    protected abstract String clientName();

    protected String extractErrorCode(final EvasysException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof SoapfaultMessage soapFault) {
            return soapFault.getFaultInfo().getSErrorMessage();
        }
        return null;
    }
}
