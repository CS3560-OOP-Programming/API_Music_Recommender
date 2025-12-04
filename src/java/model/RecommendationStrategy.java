package model;

import model.Track; //was com.musicrecommender.model.Track in the lab, should we change the layout of our project? Yeah, i think we should make it similar to the lab first and then branch off from there
import java.util.List;

/**
 * Strategy interface for different recommendation algorithms
 * Demonstrates Abstraction and Strategy Pattern
 */
public interface RecommendationStrategy {
    List<Track> recommend(List<Track> userTracks, int count);
    String getStrategyName();
}