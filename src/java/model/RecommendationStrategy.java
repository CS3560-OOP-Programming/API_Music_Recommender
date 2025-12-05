package model;

import java.util.List;

/**
 * Strategy interface for different recommendation algorithms
 * Demonstrates Abstraction and Strategy Pattern
 */
public interface RecommendationStrategy {
    List<Track> recommend(List<Track> userTracks, int count);
    String getStrategyName();
}