package caiso.esb.metrics;

import caiso.esb.config.ApplicationProperties;
import caiso.esb.config.ServiceProperties;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Expose router configuration
 */
@Service
public class RouterConfig implements Endpoint {

    @Resource
    private Environment env;
    //    @Resource
//    private CassandraConfiguration cassandraConfiguration;
//    @Resource
//    private ActiveMQConfiguration activeMQConfiguration;
    @Resource
    private ApplicationProperties appProperties;
    @Resource
    private ServiceProperties serviceProperties;

    public String[] getSpringActiveProfiles(){
        return env.getActiveProfiles();
    }
    public ApplicationProperties getApplicationProperties(){
        return appProperties;
    }
    public ServiceProperties getServiceProperties() {
        return serviceProperties;
    }

    @Override
    public String getId() {
        return "RouterConfig";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public Object invoke() {
        return this;
    }
}
