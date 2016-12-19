package caiso.esb.gateway.processor;

import caiso.camel.JMSHeader;
import caiso.esb.common.entity.ResponseStatus;
import caiso.esb.common.service.AuditDataService;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Processor to save soap message information to audit table.
 * 
 * @author akarkala
 */
@Component
public class AuditDataProcessor implements Processor {
	
    private static final Logger logger = LogManager.getLogger(AuditDataProcessor.class);
	
	@Resource
	private AuditDataService auditDataService;
	
	@Resource
	private GatewayResponseProcessor gatewayResponseProcessor;

	// public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void process(Exchange exchange) {
		Message message = exchange.getIn();
	    String soapAction = (String) message.getHeader(JMSHeader.SOAP_ACTION.toString());
		try{
		    logger.info("Auditing payload with soapaction: "+soapAction);
			auditDataService.auditRequestAndResponse(exchange,gatewayResponseProcessor.getResponse(message),0,ResponseStatus.SUCCESS, true);
		}catch(Throwable e){//Log and ignore. Auditing cannot stop execution of main application
			logger.error("Error processing message with soapaction :"+soapAction,e);
		}
	}
}
