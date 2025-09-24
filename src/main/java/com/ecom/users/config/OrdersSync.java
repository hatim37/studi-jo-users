package com.ecom.users.config;

import com.ecom.users.clients.OrdersRestClient;
import com.ecom.users.service.TokenTechnicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdersSync {

    private final OrdersRestClient orderRestClient;
    private final TokenTechnicService tokenTechnicService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            String token = "Bearer " + tokenTechnicService.getTechnicalToken();
            orderRestClient.synchronizeOrders(token);
            log.info("Synchronisation avec Orders demandée au démarrage.");
        } catch (Exception e) {
            log.error("Impossible de demander la synchro à Orders au démarrage", e);
        }
    }
}
