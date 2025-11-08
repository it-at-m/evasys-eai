package de.muenchen.evasys.soap;

import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;
import javax.xml.namespace.QName;

public class SoapHeaderHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String NAMESPACE_URI = "soapserver-v100.wsdl";
    private final String username;
    private final String password;

    public SoapHeaderHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean isOutbound = Boolean.TRUE.equals(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        if (isOutbound) {
            addAuthenticationHeader(context);
        }
        return true;
    }

    private void addAuthenticationHeader(SOAPMessageContext context) {
        try {
            SOAPMessage soapMsg = context.getMessage();
            SOAPEnvelope envelope = soapMsg.getSOAPPart().getEnvelope();
            SOAPHeader header = envelope.getHeader() != null ? envelope.getHeader() : envelope.addHeader();
            QName qname = new QName(NAMESPACE_URI, "Header");
            var headerElement = header.addChildElement(qname);
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
        return null;
    }
}
