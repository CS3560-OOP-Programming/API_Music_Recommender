package com.musicrecommender.recommendation;

import com.musicrecommender.model.Track;
import java.util.List;

/**
 * Main recommendation engine that uses different strategies
 * Demonstrates Composition and Strategy Pattern
 */
public class RecommendationEngine {
    private RecommendationStrategy strategy;

    public RecommendationEngine(RecommendationStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(RecommendationStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Track> getRecommendations(List<Track> userTracks, int count) {
        System.out.println("Using strategy: " + strategy.getStrategyName());
        return strategy.recommend(userTracks, count);
    }
}