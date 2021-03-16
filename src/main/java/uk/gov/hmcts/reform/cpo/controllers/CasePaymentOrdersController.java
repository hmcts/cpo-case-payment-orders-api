package uk.gov.hmcts.reform.cpo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class CasePaymentOrdersController {

    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ok("Welcome to cpo-case-payment-orders-api");
    }
}
