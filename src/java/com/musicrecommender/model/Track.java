package com.musicrecommender.model;

import java.util.List;

/**
 * Represents a music track with encapsulated properties.
 * Demonstrates Encapsulation - private fields with public getters/setters
 */
public class Track {
    private String name;
    private String artist;
    private String mbid;  // MusicBrainz ID (optional unique identifier)
    private String url;
    private int listeners;
    private String imageUrl;
    private double matchScore;  // For similar tracks

    // Constructor
    public Track(String name, String artist) {
        this.name = name;
        this.artist = artist;
    }

    // Getters and Setters (Encapsulation)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getListeners() {
        return listeners;
    }

    public void setListeners(int listeners) {
        this.listeners = listeners;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    @Override
    public String toString() {
        if (matchScore > 0) {
            return String.format("%s - %s (Match: %.2f)", artist, name, matchScore);
        }
        return String.format("%s - %s", artist, name);
    }

    // Format listeners count with commas
    public String getFormattedListeners() {
        return String.format("%,d", listeners);
    }
}