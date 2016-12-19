package caiso.esb.metrics;

import caiso.esb.common.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

class ServiceFlow {
    private String serviceName;
    private String uuid;
    private boolean orderedProcessing = false;
    private Long totalTime;
    private List<String> auditLogList = new ArrayList<>();

    public ServiceFlow(String uuid){
        this.uuid = uuid;
    }

    public boolean isOrderedProcessing() {
        return orderedProcessing;
    }

    public void setOrderedProcessing(boolean orderedProcessing) {
        this.orderedProcessing = orderedProcessing;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUuid() {
        return uuid;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public String getTotalTimeString() {
        return TimeUtil.formatTime(totalTime);
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }


    public List<String> getAuditLogList() {
        return auditLogList;
    }

    @Override
    public String toString() {
        return "ServiceFlow{" +
                "serviceName='" + serviceName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", orderedProcessing=" + orderedProcessing +
                ", auditLogList=" + auditLogList +
                ", totalTime=" + totalTime +
                '}';
    }
}
