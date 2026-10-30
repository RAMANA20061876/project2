// services/RefundGateway.java
package services;

import java.math.BigDecimal;

public interface RefundGateway {

    String getName();

    // returns a reference number if the refund worked, or null if it failed
    String refund(int paymentId, BigDecimal amount);
}