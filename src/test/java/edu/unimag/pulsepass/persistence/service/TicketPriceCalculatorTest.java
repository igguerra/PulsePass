package edu.unimag.pulsepass.persistence.service;

import edu.unimag.pulsepass.persistence.domain.TicketType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TicketPriceCalculatorTest {

    private final TicketPriceCalculator calculator = new TicketPriceCalculator();

    @Test
    void deberiaCalcularPrecioBaseParaGeneral() {
        BigDecimal price = calculator.calculate(TicketType.GENERAL);

        assertThat(price).isEqualByComparingTo("100.00");
    }

    @Test
    void deberiaAplicarDescuentoParaStudent() {
        BigDecimal price = calculator.calculate(TicketType.STUDENT);

        assertThat(price).isEqualByComparingTo("70.00");
    }

    @Test
    void deberiaDuplicarPrecioParaVip() {
        BigDecimal price = calculator.calculate(TicketType.VIP);

        assertThat(price).isEqualByComparingTo("200.00");
    }

    @Test
    void deberiaCuadruplicarPrecioParaBackstage() {
        BigDecimal price = calculator.calculate(TicketType.BACKSTAGE);

        assertThat(price).isEqualByComparingTo("400.00");
    }

    @Test
    void ningunPrecioDeberiaSerNegativo() {
        for (TicketType type : TicketType.values()) {
            assertThat(calculator.calculate(type)).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }
}