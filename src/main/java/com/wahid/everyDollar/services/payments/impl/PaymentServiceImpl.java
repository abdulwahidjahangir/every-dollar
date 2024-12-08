package com.wahid.everyDollar.services.payments.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.wahid.everyDollar.errors.CustomException;
import com.wahid.everyDollar.models.payments.Payment;
import com.wahid.everyDollar.models.users.User;
import com.wahid.everyDollar.repositories.payments.PaymentRepository;
import com.wahid.everyDollar.services.payments.PaymentService;
import com.wahid.everyDollar.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.key.secret}")
    private String stripeKey;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserService userService;

    public PaymentServiceImpl() {
        Stripe.apiKey = stripeKey;
    }

    @Override
    public String getPaymentIntent(String userEmail) {
        User user = userService.getUser(userEmail);

        Optional<Payment> paymentIntent = paymentRepository.getPaymentIntent(user.getId());
        if (paymentIntent.isPresent()) {
            return paymentIntent.get().getPaymentIntent();
        }

        try {
            PaymentIntent newPaymentIntent = createPaymentIntent(userEmail, new BigDecimal("9.00"));

            Payment payment = new Payment();
            payment.setUser(user);
            payment.setAmount(new BigDecimal(9.00));
            payment.setPaymentIntent(newPaymentIntent.getClientSecret());

            paymentRepository.save(payment);

            return payment.getPaymentIntent();
        } catch (StripeException e) {
            e.printStackTrace();
            throw new CustomException("Error while creating payment intent", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new CustomException("Error while creating payment intent 2", HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @Override
    public void confirmPayment(String userEmail, String paymentId) {
        User user = userService.getUser(userEmail);

        try {
            boolean verifyPayment = verifyPayment(paymentId);
            if (verifyPayment) {
                Payment payment = paymentRepository.verifyPayment(user.getId(), paymentId)
                        .orElseThrow(() -> new Exception("Error while verifying payment"));

                payment.setHasPaid(true);

                paymentRepository.save(payment);
            }
        } catch (Exception e) {
            throw new CustomException("Error while confirming payment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PaymentIntent createPaymentIntent(String userEmail, BigDecimal amount) throws StripeException {
        List<String> paymentMethodsType = Collections.singletonList("card");

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
        params.put("currency", "USD");
        params.put("payment_method_types", paymentMethodsType);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("userEmail", userEmail);
        params.put("metadata", metadata);

        return PaymentIntent.create(params);
    }

    private boolean verifyPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        return "succeeded".equals(paymentIntent.getStatus());
    }
}
