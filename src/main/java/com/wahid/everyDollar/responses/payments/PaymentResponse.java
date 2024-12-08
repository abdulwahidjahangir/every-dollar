package com.wahid.everyDollar.responses.payments;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponse {
    private String paymentId;
    private boolean hasPaid;
    private BigDecimal amount;
    private LocalDateTime paidAt;
}
