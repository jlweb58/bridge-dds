package com.webber.bridge_dds.controller;


import java.util.List;

/**
 * @param cards A bridge hand in "SA" string format
 */
public record HandEvaluationRequest(List<String> cards) {
}
