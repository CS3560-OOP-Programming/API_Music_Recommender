/* Need to change these after we put all packages n stuff in order
package com.musicrecommender.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.musicrecommender.api.SpotifyAPIClient;
import com.musicrecommender.model.Track;
import com.musicrecommender.recommendation.PopularityBasedStrategy;
import com.musicrecommender.recommendation.RecommendationEngine;
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Handles individual client connections in separate threads
 * Demonstrates Multithreading and Socket communication
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final SpotifyAPIClient apiClient;
    private final Gson gson;

    public ClientHandler(Socket socket, SpotifyAPIClient apiClient) {
        this.clientSocket = socket;
        this.apiClient = apiClient;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        System.out.println("New client connected: " + clientSocket.getInetAddress());

        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Received request: " + request);
                String response = handleRequest(request);
                out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Process client requests and return JSON responses
     */
    private String handleRequest(String request) {
        try {
            JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);
            String action = jsonRequest.get("action").getAsString();

            switch (action) {
                case "SEARCH":
                    return handleSearch(jsonRequest);

                case "RECOMMEND":
                    return handleRecommend(jsonRequest);

                default:
                    return createErrorResponse("Unknown action: " + action);
            }
        } catch (Exception e) {
            return createErrorResponse("Error processing request: " + e.getMessage());
        }
    }

    private String handleSearch(JsonObject request) {
        try {
            String query = request.get("query").getAsString();
            int limit = request.has("limit") ? request.get("limit").getAsInt() : 10;

            List<Track> tracks = apiClient.searchTracks(query, limit);

            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("action", "SEARCH");
            response.add("data", gson.toJsonTree(tracks));

            return gson.toJson(response);
        } catch (IOException e) {
            return createErrorResponse("Search failed: " + e.getMessage());
        }
    }

    private String handleRecommend(JsonObject request) {
        try {
            String trackId = request.get("trackId").getAsString();
            int count = request.has("count") ? request.get("count").getAsInt() : 5;

            // Get recommendations using the engine
            List<String> seedIds = List.of(trackId);
            List<Track> recommendations = apiClient.getRecommendations(seedIds, count);

            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("action", "RECOMMEND");
            response.add("data", gson.toJsonTree(recommendations));

            return gson.toJson(response);
        } catch (IOException e) {
            return createErrorResponse("Recommendation failed: " + e.getMessage());
        }
    }

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "error");
        response.addProperty("message", message);
        return gson.toJson(response);
    }
}