package com.musicrecommender.model;
/**
 * Represents audio analysis features from Spotify API
 */

public class AudioFeatures {
    private String trackId;
    private double danceability;
    private double energy;
    private double valence;
    private double tempo;
    private double acousticness;

    public AudioFeatures(String trackId) {
        this.trackId = trackId;
    }

    // Getters and Setters
    public String getTrackId() {
        return trackId;
    }

    public double getDanceability() {
        return danceability;
    }

    public void setDanceability(double danceability) {
        this.danceability = danceability;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getValence() {
        return valence;
    }

    public void setValence(double valence) {
        this.valence = valence;
    }

    public double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        this.tempo = tempo;
    }

    public double getAcousticness() {
        return acousticness;
    }

    public void setAcousticness(double acousticness) {
        this.acousticness = acousticness;
    }
}