package com.wahid.everyDollar.services.payments;

import com.wahid.everyDollar.models.payments.Payment;

import java.util.List;

public interface PaymentService {
    String getPaymentIntent(String userEmail);

    void confirmPayment(String userEmail, String paymentId);

    List<Payment> getPaymentHistory(String userEmail);
}
