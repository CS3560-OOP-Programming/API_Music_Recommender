package service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.Track;
import org.apache.hc.core5.http.ParseException;
import model.RecommendationEngine;
import model.SimilarityBasedStrategy;
import model.RandomStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Handles individual client connections in separate threads
 * Demonstrates Multithreading and Socket communication
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final LastFmAPIClient apiClient;
    private final Gson gson;
    private final List<Track> tracksSeen = new ArrayList<>();
    private RecommendationEngine recommendationEngine;
    private SimilarityBasedStrategy similarityStrategy;
    private RandomStrategy randomStrategy;

    public ClientHandler(Socket socket, LastFmAPIClient apiClient) {
        this.clientSocket = socket;
        this.apiClient = apiClient;
        this.gson = new Gson();
        this.similarityStrategy = new SimilarityBasedStrategy(apiClient);
        this.randomStrategy = new RandomStrategy(tracksSeen);
        this.recommendationEngine = new RecommendationEngine(new SimilarityBasedStrategy(apiClient));
        //this.recommendationEngine = new RecommendationEngine(new RandomStrategy(tracksSeen));
    }

    @Override
    public void run() {
        //String fileName = "requestLog.txt";
        System.out.println("New client connected: " + clientSocket.getInetAddress());

        try (
                //FileWriter fw = new FileWriter(fileName, true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Received request: " + request);
                //fw.write(request + System.lineSeparator());
                //fw.flush();
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

                case "SET_STRATEGY":
                    return handleSetStrategy(jsonRequest);

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
            tracksSeen.addAll(tracks);

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
            String trackName = request.get("trackName").getAsString();
            String artistName = request.get("artistName").getAsString();
            int count = request.has("count") ? request.get("count").getAsInt() : 5;

            // Get similar tracks using Last.fm API
            //List<Track> recommendations = apiClient.getSimilarTracks(trackName, artistName, count);
            List<Track> recommendations = recommendationEngine.getRecommendations(List.of(new Track(trackName, artistName)), count);

            tracksSeen.addAll(recommendations);

            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("action", "RECOMMEND");
            response.add("data", gson.toJsonTree(recommendations));

            return gson.toJson(response);
        }catch(Exception e){
            return createErrorResponse("Recommendation failed: " + e.getMessage());
        }
        //} catch (IOException | ParseException e) {
            //return createErrorResponse("Recommendation failed: " + e.getMessage());
        //}
    }

    private String handleSetStrategy(JsonObject request){
        if(!request.has("strategy")){
            return createErrorResponse("There is no strategy field");
        }

        try {
            String strategy = request.get("strategy").getAsString().toLowerCase();

            switch (strategy) {
                case "similarity":
                    recommendationEngine.setStrategy(new SimilarityBasedStrategy(apiClient));
                    break;

                case "random":
                    recommendationEngine.setStrategy(new RandomStrategy(tracksSeen));
                    break;

                default:
                    return createErrorResponse("Unrecognized strategy: " + strategy);
            }

            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("message", "strategy switched to " + strategy);

            return gson.toJson(response);

        } catch (Exception e) {
            return createErrorResponse("Failed to set strategy: " + e.getMessage());
        }
    }

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "error");
        response.addProperty("message", message);
        return gson.toJson(response);
    }
}