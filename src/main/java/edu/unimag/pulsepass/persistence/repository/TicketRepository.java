package edu.unimag.pulsepass.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.unimag.pulsepass.persistence.domain.Ticket;
import edu.unimag.pulsepass.persistence.domain.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // FR-TKT-006: tickets de un usuario, navegando Ticket -> User -> email
    List<Ticket> findByUserEmailIgnoreCase(String email);

    // FR-TKT-006: tickets de un usuario, opcionalmente filtrados por estado
    List<Ticket> findByUserEmailIgnoreCaseAndStatus(String email, TicketStatus status);

    // FR-TKT-007: tickets PAID (o cualquier estado) de un evento, por eventCode
    List<Ticket> findByEventEventCodeAndStatus(String eventCode, TicketStatus status);

    // FR-TKT-008 / AC-008: conteo de tickets PAID de un evento (solo cuentan los PAID)
    @Query("""
            SELECT COUNT(t) FROM Ticket t
            WHERE t.event.eventCode = :eventCode
              AND t.status = edu.unimag.pulsepass.persistence.domain.TicketStatus.PAID
            """)
    long countPaidTicketsByEventCode(@Param("eventCode") String eventCode);

    // FR-SRC-004: tickets de eventos posteriores a una fecha, ordenados cronologicamente
    @Query("""
            SELECT t FROM Ticket t
            WHERE t.event.eventDate > :fromDate
            ORDER BY t.event.eventDate ASC
            """)
    List<Ticket> findTicketsForUpcomingEvents(@Param("fromDate") LocalDateTime fromDate);

}
