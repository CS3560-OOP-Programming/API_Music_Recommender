package service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.Track;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Handles socket connection to the server
 * Demonstrates Client Socket programming
 */
public class ServerConnection {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson;

    public ServerConnection() {
        this.gson = new Gson();
    }

    /**
     * Connect to the server
     */
    public void connect() throws IOException {
        socket = new Socket(Config.SERVER_HOST, Config.SERVER_PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connected to server");
    }

    /**
     * Search for tracks on the server
     */
    public List<Track> searchTracks(String query) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", "SEARCH");
        request.addProperty("query", query);
        request.addProperty("limit", 20);

        return sendRequest(request);
    }

    /**
     * Get recommendations from the server
     */
    public List<Track> getRecommendations(String trackName, String artistName) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", "RECOMMEND");
        request.addProperty("trackName", trackName);
        request.addProperty("artistName", artistName);
        request.addProperty("count", 10);

        return sendRequest(request);
    }

    private List<Track> sendRequest(JsonObject request) throws IOException {
        // Send request
        out.println(gson.toJson(request));

        // Receive response
        String responseLine = in.readLine();
        JsonObject response = gson.fromJson(responseLine, JsonObject.class);

        if (response.get("status").getAsString().equals("success")) {
            // Parse tracks from response
            Track[] tracksArray = gson.fromJson(
                    response.get("data"), Track[].class);
            return List.of(tracksArray);
        } else {
            String errorMsg = response.get("message").getAsString();
            throw new IOException("Server error: " + errorMsg);
        }
    }

    /**
     * Close the connection
     */
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}