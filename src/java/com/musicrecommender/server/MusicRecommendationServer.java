package com.musicrecommender.server;

import com.musicrecommender.api.LastFmAPIClient;
import com.musicrecommender.config.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-threaded server that handles multiple client connections
 * Demonstrates Server Socket programming and Thread Pool
 */
public class MusicRecommendationServer {
    private final int port;
    private final LastFmAPIClient apiClient;
    private final ExecutorService threadPool;
    private volatile boolean running;

    public MusicRecommendationServer(int port) {
        this.port = port;
        this.apiClient = new LastFmAPIClient();
        this.threadPool = Executors.newFixedThreadPool(10); // Pool of 10 threads
        this.running = false;
    }

    /**
     * Start the server and listen for client connections
     */
    public void start() throws IOException {
        System.out.println("Last.fm Music Recommender Server initialized");

        running = true;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Music Recommendation Server started on port " + port);
            System.out.println("Waiting for client connections...");

            while (running) {
                // Accept incoming client connection
                Socket clientSocket = serverSocket.accept();

                // Handle client in separate thread from pool
                ClientHandler handler = new ClientHandler(clientSocket, apiClient);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            throw e;
        } finally {
            shutdown();
        }
    }

    /**
     * Shutdown the server gracefully
     */
    public void shutdown() {
        running = false;
        threadPool.shutdown();
        try {
            apiClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server shutdown complete");
    }

    public static void main(String[] args) {
        MusicRecommendationServer server = new MusicRecommendationServer(Config.SERVER_PORT);

        // Add shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}