package eu.profinit.opendata.ipfilter;

import com.google.common.collect.LinkedListMultimap;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 *
 */
public class IpTimeWindowManager {


    public static final int WINDOW_SIZE_IN_MINUTES = 1;
    public static final int MAX_REQUEST_PER_IP_IN_WINDOW = 50;
    private long lastEpochMinute;
    String ip = new String();

    private LinkedListMultimap<String, Long> requestsPerIp;

    public IpTimeWindowManager() {
        requestsPerIp = LinkedListMultimap.create();
        lastEpochMinute = 0;
    }

    public synchronized void addIpRequest(String ipAddress) {
        long epochSecond = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        requestsPerIp.put(ipAddress, epochSecond);
        ip = ipAddress;

        long epochMinute = epochSecond - (epochSecond % 60);
        if (epochMinute > lastEpochMinute) {
            lastEpochMinute = epochMinute;
            cleanExpiredRequests();
        }
    }

    private void cleanExpiredRequests() {
        long expiredEpochMinute = lastEpochMinute - (WINDOW_SIZE_IN_MINUTES * 60);

        for (String ipAddress : requestsPerIp.keySet()) {
            List<Long> requests = requestsPerIp.get(ipAddress);

            for (Long request : requests) {
                if (request < expiredEpochMinute) {
                    requests.remove(request);
                }
            }

            if (requests.isEmpty()) {
                requestsPerIp.removeAll(ipAddress);
            }

        }
    }

    public synchronized boolean ipAddressReachedLimit(String ipAddress) {
        int amountRequests = requestsPerIp.get(ipAddress).size();
        return (amountRequests > MAX_REQUEST_PER_IP_IN_WINDOW);
    }

    public  String getIp () {
        return ip;
    }
}