package com.wahid.everyDollar.services.payments;

public interface PaymentService {
    String getPaymentIntent(String userEmail);

    void confirmPayment(String userEmail, String paymentId);
}
