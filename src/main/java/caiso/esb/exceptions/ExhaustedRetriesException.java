package caiso.esb.exceptions;

import caiso.esb.config.ServiceProperties;

public class ExhaustedRetriesException extends Exception {

    public ExhaustedRetriesException(ServiceProperties.ServiceConfig serviceConfig) {
        super("ERROR: Exhausted attempts for " + serviceConfig);
    }

}
