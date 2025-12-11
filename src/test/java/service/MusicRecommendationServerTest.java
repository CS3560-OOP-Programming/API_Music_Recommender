package service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

//Server tests below check if a client can connect to it using a multithreaded approach
class MusicRecommendationServerTest {
    @Test
    @DisplayName("Server-client connection")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testServerConnection() throws IOException, InterruptedException {
        Thread serverThread = new Thread(() -> {
            MusicRecommendationServer server = new MusicRecommendationServer(8888);
            try {
                server.start();
            } catch (IOException e) {
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(1000);

        try (Socket clientSocket = new Socket("localhost", 8888)) {
            assertTrue(clientSocket.isConnected());
        } catch (IOException e) {
            fail("Error connecting to server");
        }
    }
    //Its not a requirement for the project, but we check here for multiple clients
    @Test
    @DisplayName("Test 3 clients")
    void testMultipleConnections() throws InterruptedException {
        ExecutorService clientPool = Executors.newFixedThreadPool(3);
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int clientId = i;
            futures.add(clientPool.submit(() -> {
                try (Socket socket = new Socket("localhost", Config.SERVER_PORT);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    out.println("SEARCH:Test Song " + clientId);
                    return in.readLine();
                } catch (IOException e) {
                    return "ERROR: " + e.getMessage();
                }
            }));
        }
    }
}