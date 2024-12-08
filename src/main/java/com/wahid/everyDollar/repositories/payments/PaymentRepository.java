package com.wahid.everyDollar.repositories.payments;

import com.wahid.everyDollar.models.payments.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.hasPaid = false")
    Optional<Payment> getPaymentIntent(Long userId);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId")
    List<Payment> paymentHistory(Long userId);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.hasPaid = false AND p.paymentIntent = :paymentIntent")
    Optional<Payment> verifyPayment(Long userId, String paymentIntent);
}
