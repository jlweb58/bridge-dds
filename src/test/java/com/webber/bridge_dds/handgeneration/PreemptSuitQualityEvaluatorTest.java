package com.webber.bridge_dds.handgeneration;

import com.webber.bridge_dds.model.Rank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreemptSuitQualityEvaluatorTest {

    private PreemptSuitQualityEvaluator evaluator;
    @BeforeEach
    public void setUp() {
        Resource resource = new ClassPathResource("/expected-tricks.csv");
        evaluator = new PreemptSuitQualityEvaluator(resource);
    }

    @Test
    public void testQueenCombinations() {
        Set<Rank> combo1 = EnumSet.of(Rank.QUEEN, Rank.JACK, Rank.TEN);
        Set<Rank> combo2 = EnumSet.of(Rank.QUEEN, Rank.TEN, Rank.NINE);
        Set<Rank> combo3 = EnumSet.of(Rank.QUEEN, Rank.NINE, Rank.EIGHT);
        Set<Rank> combo4 = EnumSet.of(Rank.QUEEN, Rank.EIGHT, Rank.SEVEN);

        double result1 = evaluator.evaluate(combo1);
        double result2 = evaluator.evaluate(combo2);
        double result3 = evaluator.evaluate(combo3);
        double result4 = evaluator.evaluate(combo4);

        assertTrue(result1 > result2 && result2 > result3 && result3 > result4);
    }

    @Test
    public void testKingQueenCombinations() {
        Set<Rank> combo1 = EnumSet.of(Rank.KING, Rank.QUEEN, Rank.JACK);
        Set<Rank> combo2 = EnumSet.of(Rank.KING, Rank.QUEEN, Rank.TEN);
        Set<Rank> combo3 = EnumSet.of(Rank.KING, Rank.QUEEN, Rank.NINE);
        Set<Rank> combo4 = EnumSet.of(Rank.KING, Rank.QUEEN, Rank.EIGHT);

        double result1 = evaluator.evaluate(combo1);
        double result2 = evaluator.evaluate(combo2);
        double result3 = evaluator.evaluate(combo3);
        double result4 = evaluator.evaluate(combo4);

        assertTrue(result1 > result2 && result2 > result3 && result3 > result4);
    }

    @Test
    public void testKingQueenOrJackCombinations() {
        Set<Rank> combo1 = EnumSet.of(Rank.KING, Rank.JACK, Rank.TEN);
        Set<Rank> combo2 = EnumSet.of(Rank.KING, Rank.QUEEN, Rank.EIGHT);
        Set<Rank> combo3 = EnumSet.of(Rank.KING, Rank.JACK, Rank.SEVEN);
        Set<Rank> combo4 = EnumSet.of(Rank.KING, Rank.TEN, Rank.NINE);


    }


}
