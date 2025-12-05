package com.musicrecommender.recommendation;

import com.musicrecommender.api.LastFmAPIClient;
import com.musicrecommender.model.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Recommends tracks based on Last.fm's similarity algorithm
 * Demonstrates Polymorphism - implements RecommendationStrategy
 */
public class SimilarityBasedStrategy implements RecommendationStrategy {
    private final LastFmAPIClient apiClient;

    public SimilarityBasedStrategy(LastFmAPIClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<Track> recommend(List<Track> userTracks, int count) {
        try {
            // Use first track as seed
            if (userTracks.isEmpty()) {
                return new ArrayList<>();
            }

            Track seedTrack = userTracks.get(0);
            return apiClient.getSimilarTracks(seedTrack.getName(), seedTrack.getArtist(), count);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public String getStrategyName() {
        return "Similarity-Based Recommendations (Last.fm)";
    }
}