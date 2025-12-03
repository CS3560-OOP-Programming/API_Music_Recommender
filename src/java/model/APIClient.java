package model;

// Still have to adjust these import statements as
// our directory structure is different from the lab's
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.musicrecommender.config.Config;
import com.musicrecommender.model.Track;
import com.musicrecommender.model.Artist;
import com.musicrecommender.model.AudioFeatures;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * REST API Client for Spotify Web API
 * Demonstrates REST API calls and JSON parsing
 */

public class APIClient {
    private String accessToken;
    private final CloseableHttpClient httpClient;

    public APIClient() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Authenticate with Spotify API using Client Credentials Flow
     * Demonstrates REST POST request
     */

    public void authenticate() throws IOException {
        String auth = Config.CLIENT_ID + ":" + Config.CLIENT_SECRET;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpPost httpPost = new HttpPost(Config.TOKEN_URL);
        httpPost.setHeader("Authorization", "Basic " + encodedAuth);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new StringEntity("grant_type=client_credentials"));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            this.accessToken = jsonObject.get("access_token").getAsString();
            System.out.println("Successfully authenticated with Spotify API");
        }
    }

    /**
     * Search for tracks by query
     * Demonstrates REST GET request and JSON parsing
     */
    public List<Track> searchTracks(String query, int limit) throws IOException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s/search?q=%s&type=track&limit=%d",
                Config.API_BASE_URL, encodedQuery, limit);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            return parseTracksFromJson(jsonResponse);
        }
    }

    /**
     * Parse Track objects from JSON response
     * Demonstrates JSON parsing and object creation
     */
    private List<Track> parseTracksFromJson(String jsonResponse) {
        List<Track> tracks = new ArrayList<>();
        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray items = root.getAsJsonObject("tracks").getAsJsonArray("items");

        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();

            // Extract track information
            String id = item.get("id").getAsString();
            String name = item.get("name").getAsString();

            // Extract artists
            List<String> artists = new ArrayList<>();
            JsonArray artistsArray = item.getAsJsonArray("artists");
            for (int j = 0; j < artistsArray.size(); j++) {
                artists.add(artistsArray.get(j).getAsJsonObject().get("name").getAsString());
            }

            // Extract album name
            String albumName = item.getAsJsonObject("album").get("name").getAsString();

            // Create Track object
            Track track = new Track(id, name, artists, albumName);
            track.setDurationMs(item.get("duration_ms").getAsInt());
            track.setPopularity(item.get("popularity").getAsInt());

            if (!item.get("preview_url").isJsonNull()) {
                track.setPreviewUrl(item.get("preview_url").getAsString());
            }

            tracks.add(track);
        }

        return tracks;
    }

    /**
     * Get recommendations based on seed tracks
     */
    public List<Track> getRecommendations(List<String> seedTrackIds, int limit) throws IOException {
        String seedTracks = String.join(",", seedTrackIds);
        String url = String.format("%s/recommendations?seed_tracks=%s&limit=%d",
                Config.API_BASE_URL, seedTracks, limit);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray tracks = root.getAsJsonArray("tracks");
            return parseTracksFromJsonArray(tracks);
        }
    }

    private List<Track> parseTracksFromJsonArray(JsonArray tracksArray) {
        List<Track> tracks = new ArrayList<>();

        for (int i = 0; i < tracksArray.size(); i++) {
            JsonObject item = tracksArray.get(i).getAsJsonObject();

            String id = item.get("id").getAsString();
            String name = item.get("name").getAsString();

            List<String> artists = new ArrayList<>();
            JsonArray artistsArray = item.getAsJsonArray("artists");
            for (int j = 0; j < artistsArray.size(); j++) {
                artists.add(artistsArray.get(j).getAsJsonObject().get("name").getAsString());
            }

            String albumName = item.getAsJsonObject("album").get("name").getAsString();

            Track track = new Track(id, name, artists, albumName);
            track.setDurationMs(item.get("duration_ms").getAsInt());
            track.setPopularity(item.get("popularity").getAsInt());

            tracks.add(track);
        }

        return tracks;
    }

    /**
     * Get audio features for a track
     */
    public AudioFeatures getAudioFeatures(String trackId) throws IOException {
        String url = String.format("%s/audio-features/%s", Config.API_BASE_URL, trackId);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            AudioFeatures features = new AudioFeatures(trackId);
            features.setDanceability(json.get("danceability").getAsDouble());
            features.setEnergy(json.get("energy").getAsDouble());
            features.setValence(json.get("valence").getAsDouble());
            features.setTempo(json.get("tempo").getAsDouble());
            features.setAcousticness(json.get("acousticness").getAsDouble());

            return features;
        }
    }

    public void close() throws IOException {
        httpClient.close();
    }
}
