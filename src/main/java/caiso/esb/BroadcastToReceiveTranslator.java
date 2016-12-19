package caiso.esb;

import caiso.camel.JMSHeader;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class BroadcastToReceiveTranslator {

    private static final Logger logger = LogManager.getLogger(BroadcastToReceiveTranslator.class);

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void translate(Exchange exchange) throws IOException {
        // convenience method to copy all headers and such to out message
        exchange.setOut(exchange.getIn().copy());
        Message replyMessage = exchange.getOut();

        /*
        transform SOAPAction http header
        set SERVICE_NAME from SOAPAction
        */
        // e.g., "http://www.caiso.com/soa/2012-04-01/broadcastTelemetryData_v20120401"
        String origSOAPAction = (String) replyMessage.getHeader(JMSHeader.SOAP_ACTION.toString());
        replyMessage.getHeaders().put(JMSHeader.INBOUND_SOAP_ACTION.toString(), origSOAPAction);

        // e.g., broadcastTelemetryData_v20120401 -> receiveTelemetryData_v20120401
        String newSoapAction = origSOAPAction.replaceAll("broadcast", "receive");
        replyMessage.getHeaders().put(JMSHeader.SOAP_ACTION.toString(), newSoapAction);

        logger.trace("[{}] : Translate SOAPAction {} -> {}", origSOAPAction, newSoapAction);

        /*
         transform SOAP message from broadcast -> receive
         */
        byte[] jmsMessageData = getBytes(replyMessage);
        String origBody = new String(jmsMessageData);
        logger.trace("head of origBody {}", origBody.substring(0, Math.min(500, origBody.length())));
        String newBody = origBody.replaceAll("broadcast", "receive");
        logger.trace("head of newBody {}", newBody.substring(0, Math.min(500, origBody.length())));
        replyMessage.setBody(newBody.getBytes());

        logger.trace("Finished broadcast to receive translator for {}", origSOAPAction);
    }

    private byte[] getBytes(Message m) throws IOException {
        byte[] body = (byte[]) m.getBody();
        return body;
    }

}
