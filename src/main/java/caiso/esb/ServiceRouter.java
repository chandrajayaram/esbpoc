package caiso.esb;

import caiso.camel.AuditBean;
import caiso.camel.JMSHeader;
import caiso.esb.config.ServiceProperties;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;

/**
 * Main logic of ESB module.  Based on service configuration stored in exchange, this class routes the message to
 * the appropriate transport (JMS, HTTP) and sets the TTL.  It also handles ordered message delivery if so configured
 * for the service.
 */
@Component
public class ServiceRouter {

    private static final Logger logger = LogManager.getLogger(ServiceRouter.class);
    @Resource
    private ServiceProperties serviceProperties;
    @Resource
    private ProducerTemplate producerTemplate;
    @Resource
    private AuditBean auditBean;

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Similar to a multicast.  Create/send a new (async) message for each CAISO app listed for the exchange's service
     * configuration.  The message is posted on either (1) a distributed "FastQ" queue for unordered messages, or a
     * (2) "OrderedQ" that uses message groups to guarantee ordering with failover (but not distributed).
     *
     * @param exchange
     */
    @SuppressWarnings("unused") // used in camel
    public void multicastToDestinations(Exchange exchange) {
        Message msg = exchange.getIn();

        // e.g., "http://www.caiso.com/soa/2012-04-01/broadcastTelemetryData_v20120401"
        String inboundSoapAction = (String) msg.getHeader(JMSHeader.INBOUND_SOAP_ACTION.toString());
        ServiceProperties.ServiceConfig serviceConfig = serviceProperties.findService(inboundSoapAction);

        // ttl to use for this message
        int ttl = serviceConfig.getTTL() * 1000;

        List<ServiceProperties.AppConfig> appList = serviceConfig.getAppList();
        for (int i = 0; i < appList.size(); i++) {
            ServiceProperties.AppConfig appConfig = appList.get(i);
            Exchange exchangeCopy = exchange.copy(true); // make deep copy for new message

            // JMS or HTTP enum values - set destination
            exchangeCopy.getIn().setHeader(JMSHeader.SERVICE_APP_CONFIG_TRANSPORT.toString(), appConfig.getTransport().name());
            String dest = null;
            switch (appConfig.getTransport()) {
                case HTTP:
                    // replace protocol in dest URL - probably there's a better way to do this
                    dest = appConfig.getDestination();
                    dest = dest.replaceAll("\\{\\{receiveAIBaseURL\\}\\}", serviceProperties.getReceiveAIBaseURL());
                    break;
                case JMS:
                    String topicName = (String) msg.getHeader(JMSHeader.SERVICE_NAME.toString());
                    dest = "activemq:topic:" + topicName + "?timeToLive=" + ttl;
                    break;
            }

            /*
            NOTE: Nested properties don't work well in camel, so set flat properties
             */
            // store either HTTP url or JMS topic name
            exchangeCopy.getIn().setHeader(JMSHeader.SERVICE_APP_CONFIG_DESTINATION.toString(), dest);
            // retry count on errors
            exchangeCopy.getIn().setHeader(JMSHeader.SERVICE_APP_CONFIG_RETRIES.toString(), appConfig.getRetries());

            if (serviceConfig.isOrderedProcessing()) {

                /*
                To enforce 'exclusive consumer' and thereby ordered processing, set special header
                    see: http://activemq.apache.org/message-groups.html
                 */
//                exchangeCopy.getIn().setHeader("JMSXGroupID", inboundSoapAction);
                //bs2: testing msg group header to be receive/app name
                exchangeCopy.getIn().setHeader("JMSXGroupID", appConfig.getDestination() );
                auditBean.logTime("caiso.esb.audit.router.orderedQ.in", exchangeCopy);

                // now lock this serviceName to a single consumer
                producerTemplate.asyncSend("activemq:queue:caiso.esb.orderedQ?timeToLive=" + ttl, exchangeCopy);
            } else {
                auditBean.logTime("caiso.esb.audit.router.fastQ.in", exchangeCopy);

                producerTemplate.asyncSend("activemq:queue:caiso.esb.fastQ?timeToLive=" + ttl, exchangeCopy);
            }
        }
    }

    @PostConstruct
    public void init() {
    }

    @PreDestroy
    public void destroy() {
        try {
            producerTemplate.stop();
        } catch (Exception e) {
            logger.error(e);
        }
    }

}
