package edu.unimag.pulsepass.persistence.service;

import edu.unimag.pulsepass.persistence.domain.TicketType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TicketPriceCalculator {

    private static final BigDecimal BASE_PRICE = new BigDecimal("100.00");
    private static final BigDecimal STUDENT_DISCOUNT = new BigDecimal("0.70");
    private static final BigDecimal VIP_MULTIPLIER = new BigDecimal("2.00");
    private static final BigDecimal BACKSTAGE_MULTIPLIER = new BigDecimal("4.00");

    public BigDecimal calculate(TicketType type) {
        BigDecimal price = switch (type) {
            case GENERAL -> BASE_PRICE;
            case STUDENT -> BASE_PRICE.multiply(STUDENT_DISCOUNT);
            case VIP -> BASE_PRICE.multiply(VIP_MULTIPLIER);
            case BACKSTAGE -> BASE_PRICE.multiply(BACKSTAGE_MULTIPLIER);
        };

        return price.setScale(2, RoundingMode.HALF_UP);
    }
}