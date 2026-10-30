// services/MockRefundGateway.java
package services;

import java.math.BigDecimal;

// Only a simulation. No real money is refunded.
public class MockRefundGateway implements RefundGateway {

    public String getName() {
        return "MOCK";
    }

    public String refund(int paymentId, BigDecimal amount) {
        return "MOCK-" + paymentId + "-" + System.currentTimeMillis();
    }
}