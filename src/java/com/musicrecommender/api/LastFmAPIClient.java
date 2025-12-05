package com.musicrecommender.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.musicrecommender.config.Config;
import com.musicrecommender.model.Track;
import com.musicrecommender.model.Artist;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * REST API Client for Last.fm Web API
 * Demonstrates REST API calls and JSON parsing
 */
public class LastFmAPIClient {
    private final CloseableHttpClient httpClient;

    public LastFmAPIClient() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Search for tracks by query
     * Demonstrates REST GET request and JSON parsing
     */
    public List<Track> searchTracks(String query, int limit) throws IOException, ParseException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s?method=track.search&track=%s&api_key=%s&format=json&limit=%d",
                Config.API_BASE_URL, encodedQuery, Config.API_KEY, limit);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "MusicRecommenderLab/1.0");

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            return parseTracksFromSearch(jsonResponse);
        }
    }

    /**
     * Parse Track objects from search JSON response
     * Demonstrates JSON parsing and object creation
     */
    private List<Track> parseTracksFromSearch(String jsonResponse) {
        List<Track> tracks = new ArrayList<>();
        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if (!root.has("results")) {
            return tracks;
        }

        JsonObject results = root.getAsJsonObject("results");
        if (!results.has("trackmatches")) {
            return tracks;
        }

        JsonObject trackmatches = results.getAsJsonObject("trackmatches");
        if (!trackmatches.has("track")) {
            return tracks;
        }

        JsonElement trackElement = trackmatches.get("track");
        JsonArray trackArray;

        // Handle both array and single object cases
        if (trackElement.isJsonArray()) {
            trackArray = trackElement.getAsJsonArray();
        } else {
            trackArray = new JsonArray();
            trackArray.add(trackElement);
        }

        for (int i = 0; i < trackArray.size(); i++) {
            JsonObject item = trackArray.get(i).getAsJsonObject();

            String name = item.get("name").getAsString();
            String artist = item.get("artist").getAsString();

            Track track = new Track(name, artist);

            if (item.has("url")) {
                track.setUrl(item.get("url").getAsString());
            }

            if (item.has("listeners")) {
                try {
                    track.setListeners(Integer.parseInt(item.get("listeners").getAsString()));
                } catch (NumberFormatException e) {
                    track.setListeners(0);
                }
            }

            // Extract image URL (medium size)
            if (item.has("image")) {
                JsonArray images = item.getAsJsonArray("image");
                for (JsonElement imgElement : images) {
                    JsonObject img = imgElement.getAsJsonObject();
                    if (img.get("size").getAsString().equals("medium")) {
                        track.setImageUrl(img.get("#text").getAsString());
                        break;
                    }
                }
            }

            tracks.add(track);
        }

        return tracks;
    }

    /**
     * Get similar tracks based on a given track
     * This is our recommendation engine!
     */
    public List<Track> getSimilarTracks(String trackName, String artistName, int limit) throws IOException {
        String encodedTrack = URLEncoder.encode(trackName, StandardCharsets.UTF_8);
        String encodedArtist = URLEncoder.encode(artistName, StandardCharsets.UTF_8);
        String url = String.format("%s?method=track.getSimilar&artist=%s&track=%s&api_key=%s&format=json&limit=%d",
                Config.API_BASE_URL, encodedArtist, encodedTrack, Config.API_KEY, limit);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "MusicRecommenderLab/1.0");

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            return parseSimilarTracks(jsonResponse);
        }
    }

    /**
     * Parse similar tracks from JSON response
     */
    private List<Track> parseSimilarTracks(String jsonResponse) {
        List<Track> tracks = new ArrayList<>();
        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if (!root.has("similartracks")) {
            return tracks;
        }

        JsonObject similarTracks = root.getAsJsonObject("similartracks");
        if (!similarTracks.has("track")) {
            return tracks;
        }

        JsonElement trackElement = similarTracks.get("track");
        JsonArray trackArray;

        if (trackElement.isJsonArray()) {
            trackArray = trackElement.getAsJsonArray();
        } else {
            trackArray = new JsonArray();
            trackArray.add(trackElement);
        }

        for (int i = 0; i < trackArray.size(); i++) {
            JsonObject item = trackArray.get(i).getAsJsonObject();

            String name = item.get("name").getAsString();

            // Artist can be an object or a string
            String artist;
            JsonElement artistElement = item.get("artist");
            if (artistElement.isJsonObject()) {
                artist = artistElement.getAsJsonObject().get("name").getAsString();
            } else {
                artist = artistElement.getAsString();
            }

            Track track = new Track(name, artist);

            // Get match score
            if (item.has("match")) {
                try {
                    track.setMatchScore(Double.parseDouble(item.get("match").getAsString()));
                } catch (NumberFormatException e) {
                    track.setMatchScore(0.0);
                }
            }

            if (item.has("url")) {
                track.setUrl(item.get("url").getAsString());
            }

            // Extract image URL
            if (item.has("image")) {
                JsonArray images = item.getAsJsonArray("image");
                for (JsonElement imgElement : images) {
                    JsonObject img = imgElement.getAsJsonObject();
                    if (img.get("size").getAsString().equals("medium")) {
                        track.setImageUrl(img.get("#text").getAsString());
                        break;
                    }
                }
            }

            tracks.add(track);
        }

        return tracks;
    }

    /**
     * Get detailed track information
     */
    public Track getTrackInfo(String trackName, String artistName) throws IOException {
        String encodedTrack = URLEncoder.encode(trackName, StandardCharsets.UTF_8);
        String encodedArtist = URLEncoder.encode(artistName, StandardCharsets.UTF_8);
        String url = String.format("%s?method=track.getInfo&artist=%s&track=%s&api_key=%s&format=json",
                Config.API_BASE_URL, encodedArtist, encodedTrack, Config.API_KEY);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "MusicRecommenderLab/1.0");

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            return parseTrackInfo(jsonResponse);
        }
    }

    private Track parseTrackInfo(String jsonResponse) {
        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if (!root.has("track")) {
            return null;
        }

        JsonObject trackObj = root.getAsJsonObject("track");
        String name = trackObj.get("name").getAsString();

        JsonObject artistObj = trackObj.getAsJsonObject("artist");
        String artist = artistObj.get("name").getAsString();

        Track track = new Track(name, artist);

        if (trackObj.has("listeners")) {
            try {
                track.setListeners(Integer.parseInt(trackObj.get("listeners").getAsString()));
            } catch (NumberFormatException e) {
                track.setListeners(0);
            }
        }

        if (trackObj.has("url")) {
            track.setUrl(trackObj.get("url").getAsString());
        }

        return track;
    }

    public void close() throws IOException {
        httpClient.close();
    }
}