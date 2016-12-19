package caiso.esb;

import caiso.camel.JMSHeader;
import caiso.esb.config.ServiceProperties;
import caiso.esb.exceptions.NoServiceConfiguredException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Utility class to look up configuration and set some properties in the exchange for later use in workflow
 */
@Component
public class ConfigHelper {
    private static final Logger logger = LogManager.getLogger(ConfigHelper.class);

    @Resource
    private ServiceProperties serviceProperties;

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Loads service configuration for exchange's SOAPAction
     *
     * @param exchange
     */
    @SuppressWarnings("unused") //used in camel
    public void loadServiceConfig(Exchange exchange) throws NoServiceConfiguredException {
        Message msg = exchange.getIn();
        logger.trace("load service configuration for {}", msg.getBody());

        // e.g., "http://www.caiso.com/soa/2012-04-01/broadcastTelemetryData_v20120401"
        String inboundSOAPAction = (String) msg.getHeader(JMSHeader.SOAP_ACTION.toString());
        ServiceProperties.ServiceConfig serviceConfig = serviceProperties.findService(inboundSOAPAction);
        if (serviceConfig == null) {
            throw new NoServiceConfiguredException(inboundSOAPAction);
        }
        logger.trace("serviceConfig: {}", serviceConfig);

        // save the inbound soap action as we'll change it in workflow/processing
        msg.setHeader(JMSHeader.INBOUND_SOAP_ACTION.toString(), inboundSOAPAction);

        // e.g., TelemetryData_v20120401
        String serviceName = getServiceName(inboundSOAPAction);
        msg.setHeader(JMSHeader.SERVICE_NAME.toString(), serviceName);

        // flag for ordered message delivery/processing
        msg.setHeader(JMSHeader.SERVICE_ORDERED.toString(), serviceConfig.isOrderedProcessing());

    }

    private String getServiceName(String SOAPAction) {
        final String KEY = "/broadcast";
        int idx = SOAPAction.lastIndexOf(KEY);
        String result;
        if (idx >= 0) {
//            result = SOAPAction.substring(idx + KEY.length(), SOAPAction.length());
            result = SOAPAction.substring(idx + 1, SOAPAction.length() - 1);
        } else {
            result = SOAPAction;
        }
        return result;
    }


}
