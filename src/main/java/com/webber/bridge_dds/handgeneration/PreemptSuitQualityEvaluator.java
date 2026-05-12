package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Rank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PreemptSuitQualityEvaluator {

    private final  Map<Set<Rank>, Double> expectedTricksByPattern;


    public PreemptSuitQualityEvaluator(
            @Value("classpath:expected-tricks.csv") Resource expectedTricksResource
    ) {
        this.expectedTricksByPattern = loadExpectedTricks(expectedTricksResource);
    }

    public double evaluate(Set<Rank> ranks) {

        return expectedTricksByPattern.entrySet().stream()
                .filter(entry -> ranks.containsAll(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .max()
                .orElse(0.0);
    }

    public double evaluateRequirement(SuitQualityRequirement requirement) {
        Double expectedTricks = expectedTricksByPattern.get(requirement.minimumRanks());
        if (expectedTricks == null) {
            throw new IllegalArgumentException("Unsupported suit quality requirement: " + requirement.minimumRanks());
        }
        return expectedTricks;
    }

    public boolean satisfies(Set<Rank> actualRanks, SuitQualityRequirement requirement) {
        return evaluate(actualRanks) >= evaluateRequirement(requirement);
    }

    private Map<Set<Rank>, Double> loadExpectedTricks(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        )) {
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(this::parseExpectedTricksRow)
                    .collect(Collectors.toUnmodifiableMap(
                            ExpectedTricksRow::ranks,
                            ExpectedTricksRow::expectedTricks
                    ));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load preempt suit quality table", e);
        }
    }

    private ExpectedTricksRow parseExpectedTricksRow(String line) {
        String[] parts = line.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid expected-tricks row: " + line);
        }

        return new ExpectedTricksRow(
                parseRanks(parts[0].trim()),
                Double.parseDouble(parts[1].trim())
        );
    }

    private Set<Rank> parseRanks(String text) {
        EnumSet<Rank> ranks = EnumSet.noneOf(Rank.class);

        for (int i = 0; i < text.length(); i++) {
            ranks.add(parseRank(text.charAt(i)));
        }

        return Set.copyOf(ranks);
    }

    private Rank parseRank(char value) {
        return switch (Character.toUpperCase(value)) {
            case 'A' -> Rank.ACE;
            case 'K' -> Rank.KING;
            case 'Q' -> Rank.QUEEN;
            case 'J' -> Rank.JACK;
            case 'T' -> Rank.TEN;
            case '9' -> Rank.NINE;
            case '8' -> Rank.EIGHT;
            case '7' -> Rank.SEVEN;
            case '6' -> Rank.SIX;
            case '5' -> Rank.FIVE;
            case '4' -> Rank.FOUR;
            case '3' -> Rank.THREE;
            case '2' -> Rank.TWO;
            default -> throw new IllegalArgumentException("Invalid rank in suit quality pattern: " + value);
        };
    }

    private record ExpectedTricksRow(Set<Rank> ranks, double expectedTricks) {
    }



}
