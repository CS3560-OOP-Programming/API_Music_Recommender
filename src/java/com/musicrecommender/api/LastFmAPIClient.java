package com.musicrecommender.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.musicrecommender.config.Config;
import com.musicrecommender.model.Track;

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
 *
 * NO AUTHENTICATION NEEDED - Last.fm uses simple API key in URL
 */
public class LastFmAPIClient {
    private final CloseableHttpClient httpClient;

    /**
     * Constructor - No authentication needed!
     */
    public LastFmAPIClient() {
        this.httpClient = HttpClients.createDefault();
        System.out.println("Last.fm API Client initialized");
    }

    /**
     * Search for tracks by query
     * Demonstrates REST GET request and JSON parsing
     *
     * @param query The search term (track name, artist, etc.)
     * @param limit Maximum number of results to return
     * @return List of Track objects matching the search
     */
    public List<Track> searchTracks(String query, int limit) throws IOException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s?method=track.search&track=%s&api_key=%s&format=json&limit=%d",
                Config.API_BASE_URL, encodedQuery, Config.API_KEY, limit);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "MusicRecommenderLab/1.0");

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            return parseTracksFromSearch(jsonResponse);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse Track objects from search JSON response
     * Demonstrates JSON parsing and object creation
     */
    private List<Track> parseTracksFromSearch(String jsonResponse) {
        List<Track> tracks = new ArrayList<>();

        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Check for error response
            if (root.has("error")) {
                System.err.println("API Error: " + root.get("message").getAsString());
                return tracks;
            }

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
            // Last.fm returns an object if only 1 result, array if multiple
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

                if (item.has("mbid") && !item.get("mbid").getAsString().isEmpty()) {
                    track.setMbid(item.get("mbid").getAsString());
                }

                // Extract image URL (prefer medium size)
                if (item.has("image")) {
                    JsonArray images = item.getAsJsonArray("image");
                    for (JsonElement imgElement : images) {
                        JsonObject img = imgElement.getAsJsonObject();
                        String size = img.get("size").getAsString();
                        if (size.equals("medium") || size.equals("large")) {
                            String imageUrl = img.get("#text").getAsString();
                            if (!imageUrl.isEmpty()) {
                                track.setImageUrl(imageUrl);
                                break;
                            }
                        }
                    }
                }

                tracks.add(track);
            }
        } catch (Exception e) {
            System.err.println("Error parsing search results: " + e.getMessage());
            e.printStackTrace();
        }

        return tracks;
    }

    /**
     * Get similar tracks based on a given track
     * This is our recommendation engine!
     *
     * @param trackName Name of the seed track
     * @param artistName Name of the artist
     * @param limit Maximum number of recommendations
     * @return List of similar Track objects
     */
    public List<Track> getSimilarTracks(String trackName, String artistName, int limit) throws IOException, ParseException {
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

        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Check for error response
            if (root.has("error")) {
                System.err.println("API Error: " + root.get("message").getAsString());
                return tracks;
            }

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

                // Artist can be an object or a string depending on Last.fm response
                String artist;
                JsonElement artistElement = item.get("artist");
                if (artistElement.isJsonObject()) {
                    artist = artistElement.getAsJsonObject().get("name").getAsString();
                } else {
                    artist = artistElement.getAsString();
                }

                Track track = new Track(name, artist);

                // Get match score (0.0 to 1.0)
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

                if (item.has("mbid") && !item.get("mbid").getAsString().isEmpty()) {
                    track.setMbid(item.get("mbid").getAsString());
                }

                // Extract image URL
                if (item.has("image")) {
                    JsonArray images = item.getAsJsonArray("image");
                    for (JsonElement imgElement : images) {
                        JsonObject img = imgElement.getAsJsonObject();
                        String size = img.get("size").getAsString();
                        if (size.equals("medium") || size.equals("large")) {
                            String imageUrl = img.get("#text").getAsString();
                            if (!imageUrl.isEmpty()) {
                                track.setImageUrl(imageUrl);
                                break;
                            }
                        }
                    }
                }

                tracks.add(track);
            }
        } catch (Exception e) {
            System.err.println("Error parsing similar tracks: " + e.getMessage());
            e.printStackTrace();
        }

        return tracks;
    }

    /**
     * Get detailed track information
     *
     * @param trackName Name of the track
     * @param artistName Name of the artist
     * @return Track object with detailed information
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
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse detailed track info from JSON response
     */
    private Track parseTrackInfo(String jsonResponse) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Check for error response
            if (root.has("error")) {
                System.err.println("API Error: " + root.get("message").getAsString());
                return null;
            }

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

            if (trackObj.has("mbid") && !trackObj.get("mbid").getAsString().isEmpty()) {
                track.setMbid(trackObj.get("mbid").getAsString());
            }

            return track;
        } catch (Exception e) {
            System.err.println("Error parsing track info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close the HTTP client
     */
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
            System.out.println("Last.fm API Client closed");
        }
    }
}