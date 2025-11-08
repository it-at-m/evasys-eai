package de.muenchen.evasys.soap;

import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;

public class SoapHeaderHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String NAMESPACE_URI = "soapserver-v100.wsdl";
    private final String username;
    private final String password;

    public SoapHeaderHandler(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext context) {
        final boolean isOutbound = Boolean.TRUE.equals(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        if (isOutbound) {
            addAuthenticationHeader(context);
        }
        return true;
    }

    private void addAuthenticationHeader(final SOAPMessageContext context) {
        try {
            final SOAPMessage soapMsg = context.getMessage();
            final SOAPEnvelope envelope = soapMsg.getSOAPPart().getEnvelope();
            final SOAPHeader header = envelope.getHeader() != null ? envelope.getHeader() : envelope.addHeader();
            final QName qname = new QName(NAMESPACE_URI, "Header");
            final SOAPElement headerElement = header.addChildElement(qname);
            headerElement.addChildElement("Login").addTextNode(username);
            headerElement.addChildElement("Password").addTextNode(password);
            soapMsg.saveChanges();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add SOAP header", e);
        }
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }
}
