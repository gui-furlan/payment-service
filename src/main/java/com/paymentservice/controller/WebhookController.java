package com.paymentservice.controller;

import com.paymentservice.client.WebhookClient;
import com.paymentservice.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook_client")
public class WebhookController {

    private final WebhookClient webhookClient;
    private final BillingService billingService;

    public WebhookController(WebhookClient webhookClient, BillingService billingService) {
        this.webhookClient = webhookClient;
        this.billingService = billingService;
    }

    @PostMapping("/run")
    public ResponseEntity<Void> processAllPayments() {
        webhookClient.processAllPayments(billingService);
        return ResponseEntity.ok().build();
    }
}

