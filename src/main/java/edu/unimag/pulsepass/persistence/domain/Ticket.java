package edu.unimag.pulsepass.persistence.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 50)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TicketType type;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TicketStatus status;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    protected Ticket() {

    }

    public Ticket(
        String ticketCode,
        TicketType type,
        BigDecimal price,
        TicketStatus status,
        LocalDateTime purchaseDate,
        User user,
        Event event) {

            this.ticketCode = ticketCode;
            this.type = type;
            this.price = price;
            this.status = status;
            this.purchaseDate = purchaseDate;
            this.user = user;
            this.event = event;

    }

    public Long getId() { return id; }
    public String getTicketCode() { return ticketCode; }
    public TicketType getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public TicketStatus getStatus() { return status; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public User getUser() { return user; }
    public Event getEvent() { return event; }

    public void setType(TicketType type) { this.type = type; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

}
