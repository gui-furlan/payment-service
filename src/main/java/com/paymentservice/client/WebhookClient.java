package com.paymentservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.paymentservice.service.BillingService;

@Service
public class WebhookClient {

    private static final Logger log = LoggerFactory.getLogger(WebhookClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String webhookGetNextPath;
    private final String webhookPatchPath;

    public WebhookClient(
            RestTemplate restTemplate,
            @Value("${webhook.service.base-url}") String baseUrl,
            @Value("${webhook.service.getnext-path}") String webhookGetNextPath,
            @Value("${webhook.service.patch-path}") String webhookPatchPath
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.webhookGetNextPath = webhookGetNextPath;
        this.webhookPatchPath = webhookPatchPath;
    }

    /**
     * Processa todos os pagamentos disponíveis no webhook até receber 404.
     */
    public final void processAllPayments(BillingService billingService) {
        Long lastReadId = null;

        // WebhookResponse response = null;
        // response = getNextPayment();

        // log.error("response: " + response.toString());

        while (true) {
            WebhookResponse response = null;

            try {
                response = fetchNextPayment();
            } catch (RestClientException ex) {
                boolean shouldBreak = onFetchError(ex);
                if (shouldBreak) {
                    break;
                } else {
                    continue;
                }
            }

            if (shouldStop(lastReadId, response)) {
                break;
            }

            handlePayment(billingService, response);

            try {
                markWebhookAsRead(response.id());
                afterMarkAsRead(response.id());
            } catch (RestClientException ex) {
                onMarkReadError(response.id(), ex);
            }

            lastReadId = response.id();
        }
    }

    protected WebhookResponse fetchNextPayment() {
        return getNextPayment();
    }

    protected boolean shouldStop(Long lastReadId, WebhookResponse response) {
        if (response == null) {
            log.info("No payment response received, stopping");
            return true;
        }
        if (lastReadId != null && lastReadId.equals(response.id())) {
            log.info("Last read message is the same as current. Stopping loop.");
            return true;
        }
        return false;
    }

    protected void handlePayment(BillingService billingService, WebhookResponse response) {
        processNextPayment(billingService, response);
    }

    /**
     * Retorna true para encerrar o loop ao ocorrer erro de fetch.
     */
    protected boolean onFetchError(RestClientException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("404")) {
            log.info("No more payments to process (404 received)");
            return true;
        }
        log.error("Error fetching payment: {}", ex.getMessage());
        return true;
    }

    protected void afterMarkAsRead(Long id) {
        // Sem operações após a leitura.
    }

    protected void onMarkReadError(Long id, RestClientException ex) {
        log.error("Failed to mark webhook message {} as read: {}", id, ex.getMessage());
    }

    public WebhookResponse getNextPayment() {
        String url = baseUrl + webhookGetNextPath; // "http://host.docker.internal:8088/webhook/payment/next"; // 

        log.error("url: " + url);

        try {
            return restTemplate.getForObject(url, WebhookResponse.class);
        } catch (RestClientException ex) {
            log.error("Failed to fetch next payment from webhook at {}: {}", url, ex.getMessage());
            return null;
        }
    }

    /**
     * Marca a mensagem do webhook como lida via PATCH.
     */
    public void markWebhookAsRead(Long id) {
        String url = baseUrl + webhookPatchPath + "/" + id;

        try {
            restTemplate.postForObject(url, null, Void.class);
            log.info("Webhook message {} marked as read", id);
        } catch (RestClientException ex) {
            log.error("Failed to mark webhook message {} as read: {}", id, ex.getMessage());
        }
    } 

    public record WebhookResponse(
        Long id,
        String eventId,
        Double amount,
        String currency,
        String payerDocument,
        String status,
        Boolean forwarded,
        String requestPayload,
        String message,
        String forwardedResponse,
        String createdAt,
        String updatedAt
    ) { }

    /**
     * Processa o pagamento recebido do webhook, relacionando eventId ao billing e mudando status para PAID.
     */
    public void processNextPayment(BillingService billingService, WebhookResponse response) {
        try {
            java.util.UUID billingId = java.util.UUID.fromString(response.eventId().replace("pay_", ""));
            billingService.updateBillingStatus(billingId, com.paymentservice.model.enums.BillingStatus.PAID);
            log.info("Billing {} status updated to PAID", billingId);
        } catch (Exception e) {
            log.warn("Could not update billing for eventId {}: {}", response.eventId(), e.getMessage());
        }
    }
}
