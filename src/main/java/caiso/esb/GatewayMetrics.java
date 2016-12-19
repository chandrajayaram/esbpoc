package caiso.esb;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.stereotype.Component;

/**
 * Simple component to handle metrics of the service-gateway
 */
@Component
public class GatewayMetrics implements Endpoint {
    private volatile int inboundCount;
    private volatile long lastMessageMili;
    private long startupTimeMili = System.currentTimeMillis();

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public long getStartupTimeMili() {
        return startupTimeMili;
    }

    public int getInboundCount() {
        return inboundCount;
    }

    public void incInboundCount() {
        this.inboundCount++;
        lastMessageMili = System.currentTimeMillis();
    }

    public long getLastMessageMili() {
        return lastMessageMili;
    }

    @Override
    public String getId() {
        return "GatewayMetrics";
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
