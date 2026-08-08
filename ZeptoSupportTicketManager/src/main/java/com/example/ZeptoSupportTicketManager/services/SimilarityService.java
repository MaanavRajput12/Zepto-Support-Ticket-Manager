package com.example.ZeptoSupportTicketManager.services;

import com.example.ZeptoSupportTicketManager.entities.ResolvedTicket;
import com.example.ZeptoSupportTicketManager.repositories.ResolvedTicketRepository;
import com.example.ZeptoSupportTicketManager.responses.SimilarTicketResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SimilarityService {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "have",
            "i", "in", "is", "it", "my", "of", "on", "or", "order", "please", "the", "this",
            "to", "was", "were", "with", "your");

    private final ResolvedTicketRepository resolvedTicketRepository;

    public SimilarityService(ResolvedTicketRepository resolvedTicketRepository) {
        this.resolvedTicketRepository = resolvedTicketRepository;
    }

    public List<SimilarTicketResponse> findTopSimilarTickets(String description) {
        return findTopSimilarTickets(description, 3);
    }

    public List<SimilarTicketResponse> findTopSimilarTickets(String description, int limit) {
        return rankTickets(description, resolvedTicketRepository.findAll(), limit);
    }

    public List<SimilarTicketResponse> rankTickets(String query, List<ResolvedTicket> tickets, int limit) {
        if (query == null || query.isBlank() || tickets.isEmpty()) {
            return List.of();
        }

        List<List<String>> ticketTokens = tickets.stream()
                .map(ticket -> tokenize(ticket.getDescription()))
                .toList();
        List<String> queryTokens = tokenize(query);
        if (queryTokens.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> documentFrequency = new HashMap<>();
        for (List<String> tokens : ticketTokens) {
            for (String token : new HashSet<>(tokens)) {
                documentFrequency.merge(token, 1, Integer::sum);
            }
        }

        int documentCount = tickets.size();
        Map<String, Double> queryVector = tfidfVector(queryTokens, documentFrequency, documentCount);
        List<SimilarTicketResponse> responses = new ArrayList<>();

        for (int i = 0; i < tickets.size(); i++) {
            ResolvedTicket ticket = tickets.get(i);
            Map<String, Double> ticketVector = tfidfVector(ticketTokens.get(i), documentFrequency, documentCount);
            double cosine = cosineSimilarity(queryVector, ticketVector);
            double score = round(cosine * 100.0);
            responses.add(new SimilarTicketResponse(ticket.getId(), ticket.getDescription(), score,
                    ticket.getActionTaken(), ticket.getResolutionNote(), ticket.getCsat()));
        }

        return responses.stream()
                .sorted(Comparator.comparing(SimilarTicketResponse::getSimilarity).reversed()
                        .thenComparing(SimilarTicketResponse::getTicketId))
                .limit(limit)
                .toList();
    }

    private List<String> tokenize(String text) {
        if (text == null) {
            return List.of();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").split("\\s+"))
                .filter(token -> !token.isBlank())
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toList());
    }

    private Map<String, Double> tfidfVector(List<String> tokens, Map<String, Integer> documentFrequency,
            int documentCount) {
        Map<String, Long> termCounts = tokens.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        int tokenCount = tokens.size();
        Map<String, Double> vector = new HashMap<>();
        for (Map.Entry<String, Long> entry : termCounts.entrySet()) {
            double tf = entry.getValue() / (double) tokenCount;
            double idf = Math.log((documentCount + 1.0) / (documentFrequency.getOrDefault(entry.getKey(), 0) + 1.0))
                    + 1.0;
            vector.put(entry.getKey(), tf * idf);
        }
        return vector;
    }

    private double cosineSimilarity(Map<String, Double> first, Map<String, Double> second) {
        double dot = 0.0;
        for (Map.Entry<String, Double> entry : first.entrySet()) {
            dot += entry.getValue() * second.getOrDefault(entry.getKey(), 0.0);
        }
        double firstMagnitude = magnitude(first);
        double secondMagnitude = magnitude(second);
        if (firstMagnitude == 0.0 || secondMagnitude == 0.0) {
            return 0.0;
        }
        return dot / (firstMagnitude * secondMagnitude);
    }

    private double magnitude(Map<String, Double> vector) {
        return Math.sqrt(vector.values().stream().mapToDouble(value -> value * value).sum());
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
