package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Hand;
import com.webber.bridge_dds.model.Suit;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SuitQualityRequirementsValidator {

    private final PreemptSuitQualityEvaluator preemptSuitQualityEvaluator;

    public SuitQualityRequirementsValidator(PreemptSuitQualityEvaluator preemptSuitQualityEvaluator) {
        this.preemptSuitQualityEvaluator = preemptSuitQualityEvaluator;
    }

    public boolean satisfies(HandGenerationParameters parameters, Hand hand) {
        Map<Suit, SuitQualityRequirement> requirements = parameters.suitQualityRequirements();
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        if (parameters.condition() != null) {
            return conditionSatisfiesSuitQuality(parameters.condition(), requirements, hand);
        }

        return requirements.entrySet().stream()
                .allMatch(entry -> preemptSuitQualityEvaluator.satisfies(
                        hand.ranksForSuit(entry.getKey()),
                        entry.getValue()
                ));
    }

    public String qualityMode(HandGenerationParameters parameters) {
        if (parameters.suitQualityRequirements() == null || parameters.suitQualityRequirements().isEmpty()) {
            return "NONE";
        }

        return parameters.condition() == null ? "GLOBAL" : "CONDITION_MATCHING_BRANCHES";
    }

    private boolean conditionSatisfiesSuitQuality(
            HandGenerationCondition condition,
            Map<Suit, SuitQualityRequirement> requirements,
            Hand hand
    ) {
        if (condition.operator() == null) {
            int suitLength = hand.ranksForSuit(condition.suit()).size();
            boolean lengthMatches = suitLength >= condition.range().min()
                    && suitLength <= condition.range().max();

            if (!lengthMatches) {
                return false;
            }

            SuitQualityRequirement requirement = requirements.get(condition.suit());
            return requirement == null
                    || preemptSuitQualityEvaluator.satisfies(
                    hand.ranksForSuit(condition.suit()),
                    requirement
            );
        }

        return switch (condition.operator()) {
            case AND -> condition.conditions().stream()
                    .allMatch(child -> conditionSatisfiesSuitQuality(child, requirements, hand));
            case OR -> condition.conditions().stream()
                    .anyMatch(child -> conditionSatisfiesSuitQuality(child, requirements, hand));
        };
    }
}
