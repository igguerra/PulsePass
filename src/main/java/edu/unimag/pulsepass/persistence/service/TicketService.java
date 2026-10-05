package edu.unimag.pulsepass.persistence.service;

import edu.unimag.pulsepass.persistence.dto.request.PurchaseTicketRequest;
import edu.unimag.pulsepass.persistence.dto.response.TicketResponse;

import java.util.List;

public interface TicketService {

    TicketResponse purchase(PurchaseTicketRequest request);

    TicketResponse findByCode(String ticketCode);

    List<TicketResponse> findByUserEmail(String email);

    List<TicketResponse> findPaidTicketsByEvent(String eventCode);

    TicketResponse cancel(String ticketCode);

    TicketResponse markAsUsed(String ticketCode);
}