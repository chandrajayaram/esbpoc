package caiso.esb.utils;

import caiso.camel.JMSHeader;
import caiso.esb.common.utils.PayloadUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility to generate a random UUID and assign it to each payload.
 *
 * @author akarkala
 */
@Component
public class PayloadIdGenerator {
    private static final Logger logger = LogManager.getLogger(PayloadIdGenerator.class);

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void setPayloadIdHeader(Exchange exchange) {
        Message msg = exchange.getIn();
        String inboundSOAPAction = (String) msg.getHeader(JMSHeader.SOAP_ACTION.toString());
        String serviceName = PayloadUtils.getServiceNameFromSoapAction(inboundSOAPAction);
        String uuid = serviceName + "_" + UUID.randomUUID().toString();

        logger.info("Setting header {} to {} for serviceName {}", JMSHeader.PAYLOAD_ID.toString(), uuid, serviceName);
        msg.setHeader(JMSHeader.PAYLOAD_ID.toString(), uuid);
    }
}
