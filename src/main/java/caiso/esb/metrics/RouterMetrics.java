package caiso.esb.metrics;

import caiso.camel.JMSHeader;
import caiso.esb.common.utils.TimeUtil;
import org.apache.camel.Exchange;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Metrics of router auditing
 */
@Service
public class RouterMetrics implements Endpoint {
    private static HashMap<String, ServiceFlow> serviceFlowMap = new HashMap<>();
    private static HashMap<String, Long> serviceTimeMap = new HashMap<>();

    private static SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private volatile int inboundCount;
    private volatile long lastInboundMili;
    private volatile int receivesSentCount;
    private volatile long lastReceiveSentMili;
    private volatile ServiceFlow lastServiceFlow;

    private long startupTimeMili = System.currentTimeMillis();

    /**
     * Utility method to sort a map, with value being some list
     *
     * @param map        map to sort
     * @param <K>        key
     * @param <V>        valuue
     * @param maxResults max # of results to return in result
     * @return the sorted map, trimmed/compacted to passed maxResults
     */
    private static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, int maxResults) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        int count = 0;
        for (Map.Entry<K, V> entry : list) {
            if (++count > maxResults) break;
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // public ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public long getStartupTimeMili() {
        return startupTimeMili;
    }

    public ServiceFlow getLastService() {
        return lastServiceFlow;
    }

    @SuppressWarnings("unused") // used in camel
    public void completeFlow(Exchange exchange) {
        String uuid = (String) exchange.getIn().getHeader(JMSHeader.PAYLOAD_ID.toString());
        ServiceFlow serviceFlow = serviceFlowMap.get(uuid);

        long startTimeMili = (Long) exchange.getIn().getHeader("caiso.esb.audit.gatewayQ.in");

        if (serviceFlow == null) {
            serviceFlow = new ServiceFlow(uuid);
            serviceFlowMap.put(uuid, serviceFlow);

            serviceFlow.setServiceName((String) exchange.getIn().getHeader(JMSHeader.SERVICE_NAME.toString()));
            serviceFlow.setOrderedProcessing((Boolean) exchange.getIn().getHeader(JMSHeader.SERVICE_ORDERED.toString()));

            List<String> logTimeList = serviceFlow.getAuditLogList();
            logTime(exchange, "caiso.esb.audit.gatewayQ.in", null, logTimeList);
            logTime(exchange, "caiso.esb.audit.gatewayQ.out", startTimeMili, logTimeList);

            logTime(exchange, "caiso.esb.audit.router.in", startTimeMili, logTimeList);

            logTime(exchange, "caiso.esb.audit.router.config.search.start", startTimeMili, logTimeList);
            logTime(exchange, "caiso.esb.audit.router.config.search.stop", startTimeMili, logTimeList);

            logTime(exchange, "caiso.esb.audit.router.orderedQ.in", startTimeMili, logTimeList);
            logTime(exchange, "caiso.esb.audit.router.orderedQ.out", startTimeMili, logTimeList);

            logTime(exchange, "caiso.esb.audit.router.fastQ.in", startTimeMili, logTimeList);
            logTime(exchange, "caiso.esb.audit.router.fastQ.out", startTimeMili, logTimeList);

            logTime(exchange, "caiso.esb.audit.router.transport.http.in", startTimeMili, logTimeList);
            logTime(exchange, "caiso.esb.audit.router.transport.http.out", startTimeMili, logTimeList);

            logTime(exchange, "caiso.esb.audit.router.transport.jms.in", startTimeMili, logTimeList);
            logTime(exchange, "caiso.esb.audit.router.transport.jms.out", startTimeMili, logTimeList);

        } else {
            List<String> logTimeList = serviceFlow.getAuditLogList();

            // this flow was already registered, called from another thread, so add current thread's info to end of time list
            logTime(exchange, "caiso.esb.audit.router.orderedQ.in", startTimeMili, logTimeList);
            logTime(exchange, "caiso.esb.audit.router.orderedQ.out", startTimeMili, logTimeList);

            logTime(exchange, "caiso.esb.audit.router.fastQ.in", startTimeMili, logTimeList);
            logTime(exchange, "caiso.esb.audit.router.fastQ.out", startTimeMili, logTimeList);

            logTime(exchange, "caiso.esb.audit.router.transport.http.in", startTimeMili, logTimeList);
            logTime(exchange, "caiso.esb.audit.router.transport.http.out", startTimeMili, logTimeList);

        }

        /*
        use *.in instead of *.out We only care about time to AI, not from AI to app and then process and finally
        returns to AI (which gives us *.out)
         */
        //bs2: Also handle case of jms.out for JMS transport
        long finishTimeMili = (Long) exchange.getIn().getHeader("caiso.esb.audit.router.transport.http.in");
        long totalTime = finishTimeMili - startTimeMili;

        // may already have time to *.in that completed before this one, don't clobber earlier time if it's later
        Long existingTime = serviceTimeMap.get(uuid);
        if (existingTime == null || totalTime > existingTime) {
            serviceTimeMap.put(uuid, totalTime);
            serviceFlow.setTotalTime(totalTime);
        }

        this.lastServiceFlow = serviceFlow;
    }

    public Map<String, ServiceFlow> getSlowestServices() {

        Map<String, ServiceFlow> result = new HashMap();
        boolean slowOrderedFound = false, slowNormalFound = false;
        Map<String, Long> stringLongMap = sortMapByValue(serviceTimeMap, 1000);
        Iterator<String> it = stringLongMap.keySet().iterator();

        while (it.hasNext() && (!slowNormalFound || !slowOrderedFound)) {
            String key = it.next();
            ServiceFlow serviceFlow = serviceFlowMap.get(key);
            if (!slowOrderedFound && serviceFlow.isOrderedProcessing()) {
                result.put("ordered", serviceFlow);
                slowOrderedFound = true;
                continue;
            }
            if (!slowNormalFound && !serviceFlow.isOrderedProcessing()) {
                result.put("normal", serviceFlow);
                slowNormalFound = true;
                continue;
            }
        }
        return result;
    }

    public Map getSlowestServiceTimes() {
        final int MAX_RETURN = 50;

        Map<String, Long> stringLongMap = sortMapByValue(serviceTimeMap, MAX_RETURN);
        int count = 0;
        for (Iterator<Map.Entry<String, Long>> it = stringLongMap.entrySet().iterator(); it.hasNext(); ) {
            it.next();
            if (count++ < MAX_RETURN) continue;
            it.remove();
        }
        return stringLongMap;
    }

    public ServiceFlow getServeFlow(String uuid) {
        return serviceFlowMap.get(uuid);
    }

    public int getInboundCount() {
        return inboundCount;
    }

    public void incInboundCount() {
        this.inboundCount++;
        lastInboundMili = System.currentTimeMillis();
    }

    public int getReceivesSentCount() {
        return receivesSentCount;
    }

    public void incReceivesSentCount() {
        this.receivesSentCount++;
        lastReceiveSentMili = System.currentTimeMillis();
    }

    public long getLastInboundMili() {
        return lastInboundMili;
    }

    public long getLastReceiveSentMili() {
        return lastReceiveSentMili;
    }

    @Override
    public String getId() {
        return "RouterMetrics";
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

    private void logTime(Exchange exchange, String header, Long startTimeMili, List<String> log) {
        Long val = (Long) exchange.getIn().getHeader(header);
        if (val == null) return; // no header, skip logging
        if (startTimeMili == null) {
            //this is the start time / initial entry
            synchronized (this) {
                Date dt = new Date();
                dt.setTime(val);
                log.add(header + " : " + dtFormat.format(dt));
            }
        } else {
            log.add(header + " : " + TimeUtil.getElapsedTime(startTimeMili, val));
        }

    }
}
