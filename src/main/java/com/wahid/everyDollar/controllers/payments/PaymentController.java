package com.wahid.everyDollar.controllers.payments;

import com.wahid.everyDollar.models.payments.Payment;
import com.wahid.everyDollar.responses.payments.PaymentResponse;
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
import java.util.List;
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

    @GetMapping("/get-all-my-payments")
    public ResponseEntity<?> getAllMyDonations(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        List<Payment> payments = paymentService.getPaymentHistory(userDetails.getEmail());
        List<PaymentResponse> paymentResponses = payments.stream()
                .map(this::convertPaymentIntoPaymentResponse)
                .toList();

        return ResponseEntity.ok(paymentResponses);
    }

    private PaymentResponse convertPaymentIntoPaymentResponse(Payment payment) {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentId(payment.getPaymentIntent());
        paymentResponse.setAmount(payment.getAmount());
        paymentResponse.setHasPaid(payment.isHasPaid());
        if (payment.isHasPaid()) {
            paymentResponse.setPaidAt(payment.getUpdatedAt());
        } else {
            paymentResponse.setPaidAt(null);
        }
        return paymentResponse;
    }
}
