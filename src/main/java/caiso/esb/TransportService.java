package caiso.esb;


import caiso.camel.JMSHeader;
import caiso.esb.common.service.AuditDataService;
import caiso.esb.config.ServiceProperties;
import caiso.esb.exceptions.ExhaustedRetriesException;
import caiso.esb.metrics.RouterMetrics;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * Service to deliver/transport message to an endpoint.
 */
@Service
public class TransportService {
    private static final Logger logger = LogManager.getLogger(TransportService.class);

    @Resource
    private ServiceProperties serviceProperties;
    @Resource
    private ProducerTemplate producerTemplate;
    @Resource
    private RouterMetrics routerMetrics;

    @Autowired
    private AuditDataService auditDataService;

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Used ot send exchange body over an HTTP transport to destination set in {@link JMSHeader#SERVICE_APP_CONFIG_DESTINATION }
     *
     * @param exchange
     */
    @SuppressWarnings("unused") // used in camel
    public void httpTransport(Exchange exchange) throws ExhaustedRetriesException {
        Message msg = exchange.getIn();

        String dest = (String) exchange.getIn().getHeader(JMSHeader.SERVICE_APP_CONFIG_DESTINATION.toString());
        /*
        throwExceptionOnFailure=true
        Option to disable throwing the HttpOperationFailedException in case of failed responses from the remote
        server. This allows you to get all responses regardless of the HTTP status code.
         */
//        dest += "?bridgeEndpoint=true&throwExceptionOnFailure=false";
        dest += "?bridgeEndpoint=true"; // ignore exchange URI header, and use the endpoint's URI passed to producer
//        dest += "?bridgeEndpoint=true";
        dest = dest.replaceAll("http", "http4");
        // curret soap action (e.g., receive)
        String soapAction = (String) msg.getHeader(JMSHeader.SOAP_ACTION.toString());

        Exchange exchangeCopy = exchange.copy(true);

        logger.trace("Sending to {}", dest);
        long startTime = System.currentTimeMillis();
        Exchange returnedExchange = producerTemplate.send(dest, exchangeCopy);
        long endTime = System.currentTimeMillis();
        //Audit the response
        sendAuditMessage(exchange, returnedExchange, endTime-startTime,soapAction);
        if (returnedExchange.getException() == null) {
            /*
            Happy happy - successful transmission
             */
            return;
        }
        Exception exception = returnedExchange.getException();
        if (exception instanceof HttpOperationFailedException) {
            HttpOperationFailedException operationFailedException = (HttpOperationFailedException) exception;
            int statusCode = operationFailedException.getStatusCode();
            logger.error("Error sending to HTTP destination " + dest + " (code: " + statusCode + " )", exception);

            String httpMessageReply = operationFailedException.getResponseBody();
            logger.trace("Server reply " + dest + ".  Server reply: " + httpMessageReply);

        } else if (exception instanceof HttpHostConnectException) {
            HttpHostConnectException hostConnectException = (HttpHostConnectException) exception;
            logger.error("Error sending to HTTP destination " + dest + " on " + hostConnectException.getHost(), hostConnectException);

        } else {
            logger.error("Error sending to HTTP destination " + dest, exception);
        }

        // e.g., "http://www.caiso.com/soa/2012-04-01/broadcastTelemetryData_v20120401"
        String inboundSoapAction = (String) msg.getHeader(JMSHeader.INBOUND_SOAP_ACTION.toString());
        ServiceProperties.ServiceConfig serviceConfig = serviceProperties.findService(inboundSoapAction);

        int retriesLeft = (Integer) returnedExchange.getIn().getHeader(JMSHeader.SERVICE_APP_CONFIG_RETRIES.toString());
        if (retriesLeft <= 0) {
            logger.warn("No more retries left for {} transport to {}", soapAction, dest);
            throw new ExhaustedRetriesException(serviceConfig);
        } else {
            logger.info("Retrying {} with {} more retries", soapAction, retriesLeft);
            returnedExchange.getIn().setHeader(JMSHeader.SERVICE_APP_CONFIG_RETRIES.toString(), --retriesLeft);
            httpTransport(returnedExchange);
        }


    }

    @SuppressWarnings("unused") // used in camel
    public void jmsTransport(Exchange exchange) {
        //bs3: Untested JMS delivery on topic
        Message msg = exchange.getIn();

        String dest = (String) exchange.getIn().getHeader(JMSHeader.SERVICE_APP_CONFIG_DESTINATION.toString());

        // current soap action (e.g., receive)
        String soapAction = (String) msg.getHeader(JMSHeader.SOAP_ACTION.toString());
        String payloadId = (String) msg.getHeader(JMSHeader.PAYLOAD_ID.toString());

        Exchange exchangeCopy = exchange.copy(true);

        logger.trace("Sending to {}", dest);
        Exchange returnedExchange = producerTemplate.send(dest, exchangeCopy);

        logger.debug("#### serviceName : {}", exchange.getIn().getHeader(JMSHeader.SERVICE_NAME.toString()));
        logger.debug("#### destination: {}", exchange.getIn().getHeader(JMSHeader.SERVICE_APP_CONFIG_DESTINATION.toString()));

    }

    /**
     * Audit the response
     */

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

    private void sendAuditMessage(Exchange request, Exchange response, long processTime,String soapAction) {
      //  if (true) return;
        try {
            auditDataService.auditRequestAndResponse(request, response, (int)processTime,false);
            //String payloadId=(String)request.getIn().getHeader(JMSHeader.PAYLOAD_ID.toString());
            //response.getIn().setHeader(JMSHeader.PAYLOAD_ID.toString(), payloadId);
            //response.getIn().setHeader(JMSHeader.SOAP_ACTION.toString(), soapAction);
            //producerTemplate.asyncSend("activemq:caiso.esb.audit.response", response);
        } catch (Throwable e) {//Log and ignore. Auditing should not interrupt main flow
            logger.error("Error processing message with soapaction :" + soapAction, e);
        }

    }
}
