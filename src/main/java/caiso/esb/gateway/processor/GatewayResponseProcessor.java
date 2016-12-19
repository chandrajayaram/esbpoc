package caiso.esb.gateway.processor;

import caiso.camel.JMSHeader;
import caiso.esb.common.utils.PayloadUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Processor to return success response
 *
 * @author akarkala
 */
@Component
public class GatewayResponseProcessor implements Processor {

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void process(Exchange exchange) throws Exception {
        Message msg = exchange.getIn();
        // we'll copy all headers from in to out
        exchange.getOut().setHeaders(exchange.getIn().getHeaders());

        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/xml");
        exchange.getOut().setBody(getResponse(msg));

    }
    
    public String getResponse(Message msg){
        // for @@serviceName@@
        String inboundSOAPAction = (String) msg.getHeader(JMSHeader.SOAP_ACTION.toString());// e.g., "http://www.caiso.com/soa/2012-04-01/broadcastTelemetryData_v20120401"
        // for @@receiptTime@@
        Long gatewayInMili = (Long) msg.getHeader("caiso.esb.audit.gatewayQ.in");//e.g., 1475610864735
        String gatewayIn;
        synchronized (this) {
            Date dt = new Date();
            dt.setTime(gatewayInMili);
            gatewayIn = df.format(dt);
        }

        // for @@uuid@@
        String uuid = (String) msg.getHeader("caiso.esb.service.UUID");// e.g., "broadcastTelemetryDatav20120401007f939c-f42f-4521-89ab-cb7f1ed494f6")

        String response = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "   <SOAP-ENV:Body>\n" +
                "      <ns1:@@serviceName@@Response xmlns:ns1=\"http://www.caiso.com/soa/@@serviceName@@\">\n" +
                "         <returnData>\n" +
                "            <EventLog xmlns=\"http://www.caiso.com/soa/2006-06-13/StandardOutput.xsd\">\n" +
                "               <id>@@uuid@@</id>\n" +
                "               <creationTime>@@receiptTime@@</creationTime>\n" +
                "               <collectionQuantity>1</collectionQuantity>\n" +
                "               <Event>\n" +
                "                  <result>SCSS0001</result>\n" +
                "                  <id>1</id>\n" +
                "                  <name>Integration</name>\n" +
                "                  <description/>\n" +
                "                  <creationTime>@@receiptTime@@</creationTime>\n" +
                "                  <severity>INFO</severity>\n" +
                "                  <eventType>JMS</eventType>\n" +
                "               </Event>\n" +
                "               <Service>\n" +
                "                  <id>@@uuid@@</id>\n" +
                "                  <name>@@serviceName@@</name>\n" +
                "                  <description>http://www.caiso.com/soa/@@serviceName@@.wsdl</description>\n" +
                "                  <comments>Message universal unique identifier: @@uuid@@</comments>\n" +
                "               </Service>\n" +
                "            </EventLog>\n" +
                "         </returnData>\n" +
                "      </ns1:@@serviceName@@Response>\n" +
                "   </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>\n";

        String serviceName = PayloadUtils.getServiceNameFromSoapAction(inboundSOAPAction);
        response = response.replaceAll("@@serviceName@@", serviceName)
                .replaceAll("@@receiptTime@@", gatewayIn)
                .replaceAll("@@uuid@@", uuid);
        return response;    	
    }
}
