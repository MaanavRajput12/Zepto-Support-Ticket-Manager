package com.example.ZeptoSupportTicketManager.config;

import com.example.ZeptoSupportTicketManager.entities.NewTicket;
import com.example.ZeptoSupportTicketManager.entities.OrderContext;
import com.example.ZeptoSupportTicketManager.entities.ResolvedTicket;
import com.example.ZeptoSupportTicketManager.enums.ActionType;
import com.example.ZeptoSupportTicketManager.repositories.NewTicketRepository;
import com.example.ZeptoSupportTicketManager.repositories.OrderRepository;
import com.example.ZeptoSupportTicketManager.repositories.ResolvedTicketRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CsvDataImporter implements ApplicationRunner {

    private final ResolvedTicketRepository resolvedTicketRepository;
    private final NewTicketRepository newTicketRepository;
    private final OrderRepository orderRepository;

    public CsvDataImporter(ResolvedTicketRepository resolvedTicketRepository, NewTicketRepository newTicketRepository,
            OrderRepository orderRepository) {
        this.resolvedTicketRepository = resolvedTicketRepository;
        this.newTicketRepository = newTicketRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (resolvedTicketRepository.count() == 0) {
            importResolvedTickets();
        }
        if (orderRepository.count() == 0 || !orderRepository.existsById(9900L)) {
            importOrders();
        }
        importNewTickets();
    }

    private void importResolvedTickets() throws IOException {
        for (List<String> row : readCsv("data/resolved_tickets.CSV")) {
            ResolvedTicket ticket = new ResolvedTicket();
            ticket.setDescription(value(row, "description", 2));
            ticket.setActionTaken(parseAction(value(row, "resolution_action", 3)));
            ticket.setResolutionNote(value(row, "resolution_note", 4));
            ticket.setCsat(parseDouble(value(row, "csat", 6)));
            resolvedTicketRepository.save(ticket);
        }
    }

    private void importNewTickets() throws IOException {
        for (List<String> row : readCsv("data/new_tickets.CSV")) {
            Long orderId = parseLong(value(row, "order_id", 2));
            String description = value(row, "description", 3);
            if (orderId == null || description.isBlank()
                    || newTicketRepository.existsByOrderIdAndDescription(orderId, description)) {
                continue;
            }
            NewTicket ticket = new NewTicket();
            ticket.setDescription(description);
            ticket.setOrderId(orderId);
            newTicketRepository.save(ticket);
        }
    }

    private void importOrders() throws IOException {
        for (List<String> row : readCsv("data/orders_context.CSV")) {
            OrderContext order = new OrderContext();
            order.setId(parseLong(value(row, "id", 0)));
            order.setItems(value(row, "items", 1));
            order.setValue(parseDouble(value(row, "value", 2)));
            order.setDeliveryTime(value(row, "delivery_time", 3));
            order.setStatus(value(row, "status", 4));
            orderRepository.save(order);
        }
    }

    private List<List<String>> readCsv(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return List.of();
        }
        List<List<String>> rows = new ArrayList<>();
        try (InputStream inputStream = resource.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return rows;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    rows.add(parseCsvLine(line));
                }
            }
        }
        return rows;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());
        return values;
    }

    private String value(List<String> row, String ignoredName, int index) {
        return row.size() > index ? row.get(index) : "";
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.trim().replaceAll("\\D+", "");
        if (digits.isBlank()) {
            return null;
        }
        return Long.parseLong(digits);
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Double.parseDouble(value.trim());
    }

    private ActionType parseAction(String value) {
        if (value == null || value.isBlank()) {
            return ActionType.NONE;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "full_refund", "refund" -> ActionType.REFUND;
            case "redelivery" -> ActionType.REDELIVERY;
            case "coupon" -> ActionType.COUPON;
            default -> ActionType.NONE;
        };
    }
}
