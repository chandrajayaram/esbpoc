package caiso.esb.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "services")
public class ServiceProperties {
    private static final Logger logger = LogManager.getLogger(ServiceProperties.class);

    private String receiveAIBaseURL;
    private List<ServiceConfig> SOAPActionList;

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public ServiceConfig findService(String soapAction) {
        for (ServiceConfig s : SOAPActionList) {
            if (soapAction.indexOf(s.getSOAPAction()) > -1) {
                return s;
            }
        }
        return null;
    }

    public String getReceiveAIBaseURL() {
        return receiveAIBaseURL;
    }

    public void setReceiveAIBaseURL(String receiveAIBaseURL) {
        this.receiveAIBaseURL = trimTrailingSlash(receiveAIBaseURL);
    }

    public List<ServiceConfig> getSOAPActionList() {
        return SOAPActionList;
    }

    public void setSOAPActionList(List<ServiceConfig> SOAPActionList) {
        this.SOAPActionList = SOAPActionList;
    }

    @Override
    public String toString() {
        return "ServiceProperties{" +
                "receiveAIBaseURL='" + receiveAIBaseURL + '\'' +
                ", SOAPActionList=" + SOAPActionList +
                '}';
    }

    private String trimTrailingSlash(String url) {
        if (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);
        return url;
    }

    public static class ServiceConfig {
        private String SOAPAction;
        private int TTL;
        private boolean orderedProcessing = false;
        private List<AppConfig> appList;

        // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        public String getSOAPAction() {
            return SOAPAction;
        }

        public void setSOAPAction(String SOAPAction) {
            this.SOAPAction = SOAPAction;
        }

        public List<AppConfig> getAppList() {
            return appList;
        }

        public void setAppList(List<AppConfig> appList) {
            this.appList = appList;
        }

        public boolean isOrderedProcessing() {
            return orderedProcessing;
        }

        public void setOrderedProcessing(boolean orderedProcessing) {
            this.orderedProcessing = orderedProcessing;
        }

        public int getTTL() {
            return TTL;
        }

        public void setTTL(int TTL) {
            this.TTL = TTL;
        }

        @Override
        public String toString() {
            return "ServiceConfig{" +
                    "SOAPAction='" + SOAPAction + '\'' +
                    ", TTL=" + TTL +
                    ", orderedProcessing=" + orderedProcessing +
                    ", appList=" + appList +
                    '}';
        }
    }

    public enum Transport {
        JMS ("jms"),
        HTTP ("http");
        private String type;

        Transport(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Transport{" +
                    "type='" + type + '\'' +
                    '}';
        }
    }
    public static class AppConfig {

        private Transport transport = Transport.HTTP;
        private String destination = null;
        private int retries = 1;

        // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        public int getRetries() {
            return retries;
        }

        public void setRetries(int retries) {
            this.retries = retries;
        }

        public Transport getTransport() {
            return transport;
        }

        public void setTransport(Transport transport) {
            this.transport = transport;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        @Override
        public String toString() {
            return "AppConfig{" +
                    "transport=" + transport +
                    ", destination='" + destination + '\'' +
                    ", retries=" + retries +
                    '}';
        }
    }

}
