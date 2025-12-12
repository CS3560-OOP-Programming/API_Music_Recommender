package service;

public class Config {

    public static final String API_KEY = System.getenv("API_KEY");
    // API Endpoint
    public static final String API_BASE_URL = "http://ws.audioscrobbler.com/2.0/";

    // Server Configuration
    public static final int SERVER_PORT = 8888;
    public static final String SERVER_HOST = "localhost";
}