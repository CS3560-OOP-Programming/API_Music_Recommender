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
        try {
            socket = new Socket(Config.SERVER_HOST, Config.SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to server");
        } catch (IOException e) {
            System.err.println("Server connection failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
        try {
            // Send request
            out.println(gson.toJson(request));

            // Receive response
            String responseLine = in.readLine();
            //JsonObject response = gson.fromJson(responseLine, JsonObject.class);

            if (responseLine == null) {
                throw new IOException("The server disconnected");
            }

            JsonObject response = gson.fromJson(responseLine, JsonObject.class);

            if (response.get("status").getAsString().equals("success")) {
                // Parse tracks from response

                //DEBUG FIX: Add to make sure that response has data and isn't null
                if(!response.has("data") || response.get("data").isJsonNull()){
                    return List.of();
                }

                Track[] tracksArray = gson.fromJson(
                        response.get("data"), Track[].class);

                return List.of(tracksArray);
            } else {
                String errorMsg = response.get("message").getAsString();
                throw new IOException("Server error: " + errorMsg);
            }
            //Error handling: attempt to reconnect in the case of network issues
        } catch (IOException e) {
            System.err.println("Server connection got interrupted " + e.getMessage());
            e.printStackTrace();

            retryConnect();

            out.println(gson.toJson(request));
            String responseLine = in.readLine();

            if (responseLine == null) {
                throw new IOException("Retry connection failed: " + e.getMessage());
            }

            JsonObject response = gson.fromJson(responseLine, JsonObject.class);

            //Slightly repetitive, could possibly be optimized
            if (response.get("status").getAsString().equals("success")) {
                //DEBUG FIX: Add to make sure that response has data and isn't null
                if(!response.has("data") || response.get("data").isJsonNull()){
                    return List.of();
                }

                Track[] tracksArray = gson.fromJson(
                        response.get("data"), Track[].class);

                return List.of(tracksArray);
            } else {
                String errorMsg = response.get("message").getAsString();
                throw new IOException("Server error: " + errorMsg);
            }
        }
    }

    //Added so that GUI can call sendRequest without accessing the private method
    //Public wrapper method
    public void sendRequestInternal(JsonObject request){
        try{
            sendRequest(request);
        }catch(IOException e){
            System.err.println("Send request failed: " + e.getMessage());
            e.printStackTrace();
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

    public void retryConnect() throws IOException {
        // Error handling: if server disconnects mid-way through running application
        try{
            connect();
            System.out.println("Succesfully reconnected");
        }catch(IOException e){
            System.out.println("Reconnection failed: " + e.getMessage());
            throw e;
        }

    }
}