package com.wahid.everyDollar.controllers.payments;

import com.wahid.everyDollar.security.service.UserDetailsImpl;
import com.wahid.everyDollar.services.payments.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/get-payment-intent")
    public ResponseEntity<?> getPaymentIntent(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String payment = paymentService.getPaymentIntent(userDetails.getEmail());

        Map<String, Object> map = new HashMap<>();
        map.put("client-secret", payment);

        return ResponseEntity.ok(map);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestParam("payment-id") @NotNull(message = "Payment id can not be null")
            @NotEmpty(message = "Payment id can not be empty") String paymentId
    ) {
        paymentService.confirmPayment(userDetails.getEmail(), paymentId);

        return ResponseEntity.ok("Thanks for you donation");
    }
}
