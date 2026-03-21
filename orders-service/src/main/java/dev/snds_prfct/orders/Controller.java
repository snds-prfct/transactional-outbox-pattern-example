package dev.snds_prfct.orders;

import dev.snds_prfct.orders.entity.outbox.OrderOutboxEvent;
import dev.snds_prfct.orders.service.OrderOutboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Controller {

    @Autowired
    private OrderOutboxService orderOutboxService;

    @GetMapping("/outbox")
    public List<OrderOutboxEvent> find() {
        return orderOutboxService.findPendingOrderOutboxEventsBatch();
    }
}
