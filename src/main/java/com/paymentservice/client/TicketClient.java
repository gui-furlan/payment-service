package com.paymentservice.client;
import com.paymentservice.model.enums.BillingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class TicketClient {

    private static final Logger log = LoggerFactory.getLogger(TicketClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String updatePath;

    public TicketClient(
            RestTemplate restTemplate,
            @Value("${tickets.service.base-url:http://localhost:8083}") String baseUrl,
            @Value("${tickets.service.update-path:/tickets}") String updatePath
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.updatePath = updatePath;
    }

    public void updateTicketStatus(UUID ticketId, BillingStatus status) {
        String actionSegment;
        if (status == BillingStatus.PAID) {
            actionSegment = "confirm";
        } else if (status == BillingStatus.CANCELLED || status == BillingStatus.EXPIRED) {
            actionSegment = "cancel";
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Skipping ticket notification for status {} on ticket {}", status, ticketId);
            }
            return;
        }

        String url = String.format("%s%s/%s/%s", baseUrl, updatePath, ticketId, actionSegment);
        try {
            restTemplate.postForEntity(url, null, Void.class);
            if (log.isDebugEnabled()) {
                log.debug("Notified ticket service: {} -> {} via {}", ticketId, status, url);
            }
        } catch (RestClientException ex) {
            log.warn("Failed to notify ticket service at {} for ticket {} with status {}: {}",
                    url, ticketId, status, ex.getMessage());
        }
    }
}
