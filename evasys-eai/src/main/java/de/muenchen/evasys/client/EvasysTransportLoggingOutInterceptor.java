package de.muenchen.evasys.client;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvasysTransportLoggingOutInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvasysTransportLoggingOutInterceptor.class);

    private final HTTPClientPolicy httpClientPolicy;

    public EvasysTransportLoggingOutInterceptor(final HTTPClientPolicy httpClientPolicy) {
        super(Phase.PRE_STREAM);
        this.httpClientPolicy = httpClientPolicy;
    }

    @Override
    public void handleMessage(final Message message) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }

        final Object headers = message.get(Message.PROTOCOL_HEADERS);

        LOGGER.debug(
                """
                        Outgoing evasys HTTP transport:
                          method={}
                          requestUri={}
                          contentType={}
                          protocolHeaders={}
                          contentLengthHeader={}
                          transferEncodingHeader={}
                          allowChunking={}
                          chunkingThreshold={}
                          connectionTimeout={}
                          receiveTimeout={}
                        """,
                message.get(Message.HTTP_REQUEST_METHOD),
                message.get(Message.REQUEST_URI),
                message.get(Message.CONTENT_TYPE),
                headers,
                findHeader(headers, "Content-Length"),
                findHeader(headers, "Transfer-Encoding"),
                httpClientPolicy.isAllowChunking(),
                httpClientPolicy.getChunkingThreshold(),
                httpClientPolicy.getConnectionTimeout(),
                httpClientPolicy.getReceiveTimeout());
    }

    private String findHeader(final Object headers, final String headerName) {
        if (!(headers instanceof Map<?, ?> headerMap)) {
            return "<not available>";
        }

        final String searched = headerName.toLowerCase(Locale.ROOT);
        String result = "<not set>";

        for (final Map.Entry<?, ?> entry : headerMap.entrySet()) {
            if (entry.getKey() != null
                    && searched.equals(entry.getKey().toString().toLowerCase(Locale.ROOT))) {
                final Object value = entry.getValue();
                result = value instanceof List<?> values
                        ? values.toString()
                        : String.valueOf(value);
            }
        }

        return result;
    }
}
