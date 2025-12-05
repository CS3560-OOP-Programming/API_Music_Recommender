package com.musicrecommender.recommendation;

import com.musicrecommender.api.SpotifyAPIClient;
import com.musicrecommender.model.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Recommends tracks based on popularity
 * Demonstrates Polymorphism - implements RecommendationStrategy
 */
public class PopularityBasedStrategy implements RecommendationStrategy {
    private final SpotifyAPIClient apiClient;

    public PopularityBasedStrategy(SpotifyAPIClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<Track> recommend(List<Track> userTracks, int count) {
        try {
            // Use first track as seed
            if (userTracks.isEmpty()) {
                return new ArrayList<>();
            }

            List<String> seedIds = new ArrayList<>();
            seedIds.add(userTracks.get(0).getId());

            return apiClient.getRecommendations(seedIds, count);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public String getStrategyName() {
        return "Popularity-Based Recommendations";
    }
}