package view;

import model.Track;
import service.ServerConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import javax.swing.JDialog;

/**
 * Main GUI application for the Music Recommender System
 * Demonstrates Java Swing GUI components and event handling
 */
public class MusicRecommenderGUI extends JFrame {
    // GUI Components
    private JTextField searchField;
    private JButton searchButton;
    //private JButton historyButton;
    private JList<Track> searchResultsList;
    private DefaultListModel<Track> searchResultsModel;
    private JButton getRecommendationsButton;
    private JList<Track> recommendationsList;
    private DefaultListModel<Track> recommendationsModel;
    private JLabel statusLabel;
    private JButton historyButton;
    private static final String LOG_FILE = "requestLog.txt";

    // Server connection
    private ServerConnection serverConnection;

    public MusicRecommenderGUI() {
        initializeGUI();
        initializeConnection();
    }

    /**
     * Initialize GUI components
     */
    private void initializeGUI() {
        setTitle("Spotify Music Recommender System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel - Search section
        JPanel topPanel = createSearchPanel();

        // Center panel - Results and Recommendations
        JPanel centerPanel = createResultsPanel();

        // Bottom panel - Status
        JPanel bottomPanel = createStatusPanel();

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Add window listener for cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }

    /**
     * Create search panel with text field and button
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Search Music"));

        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.addActionListener(e -> performSearch());

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.addActionListener(e -> performSearch());

        panel.add(new JLabel("Search Query: "), BorderLayout.WEST);
        panel.add(searchField, BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.EAST);

        return panel;
    }

    /**
     * Create results panel with two lists
     */
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Search results panel
        JPanel searchResultsPanel = new JPanel(new BorderLayout());
        searchResultsPanel.setBorder(BorderFactory.createTitledBorder("Search Results"));

        searchResultsModel = new DefaultListModel<>();
        searchResultsList = new JList<>(searchResultsModel);
        searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultsList.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane searchScrollPane = new JScrollPane(searchResultsList);
        searchResultsPanel.add(searchScrollPane, BorderLayout.CENTER);

        getRecommendationsButton = new JButton("Get Recommendations for Selected");
        getRecommendationsButton.addActionListener(e -> getRecommendations());
        searchResultsPanel.add(getRecommendationsButton, BorderLayout.SOUTH);

        // Recommendations panel
        JPanel recommendationsPanel = new JPanel(new BorderLayout());
        recommendationsPanel.setBorder(BorderFactory.createTitledBorder("Recommendations"));

        recommendationsModel = new DefaultListModel<>();
        recommendationsList = new JList<>(recommendationsModel);
        recommendationsList.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane recommendScrollPane = new JScrollPane(recommendationsList);
        recommendationsPanel.add(recommendScrollPane, BorderLayout.CENTER);

        panel.add(searchResultsPanel);
        panel.add(recommendationsPanel);

        return panel;
    }

    /**
     * Create status panel
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.WEST);

        historyButton = new JButton("History");
        historyButton.setFont(new Font("Arial", Font.PLAIN, 12));
        historyButton.addActionListener(e -> displayLog());
        panel.add(historyButton, BorderLayout.EAST);
        return panel;
    }

    /**
     * Initialize server connection
     */
    private void initializeConnection() {
        serverConnection = new ServerConnection();
        try {
            serverConnection.connect();
            updateStatus("Connected to server");
        } catch (IOException e) {
            showError("Failed to connect to server: " + e.getMessage());
            updateStatus("Not connected");
        }
    }

    /**
     * Perform search action
     */
    private void performSearch() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            showError("Please enter a search query");
            return;
        }

        updateStatus("Searching for: " + query);
        searchButton.setEnabled(false);

        // Perform search in background thread to keep GUI responsive
        SwingWorker<List<Track>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Track> doInBackground() throws Exception {
                return serverConnection.searchTracks(query);
            }

            @Override
            protected void done() {
                try {
                    List<Track> results = get();
                    displaySearchResults(results);
                    updateStatus("Found " + results.size() + " tracks");
                } catch (Exception e) {
                    showError("Search failed: " + e.getMessage());
                    updateStatus("Search failed");
                } finally {
                    searchButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    /**
     * Display search results in the list
     */
    private void displaySearchResults(List<Track> tracks) {
        searchResultsModel.clear();
        for (Track track : tracks) {
            searchResultsModel.addElement(track);
        }

        if (!tracks.isEmpty()) {
            searchResultsList.setSelectedIndex(0);
        }
    }

    /**
     * Get recommendations for selected track
     */
    private void getRecommendations() {
        Track selectedTrack = searchResultsList.getSelectedValue();

        if (selectedTrack == null) {
            showError("Please select a track first");
            return;
        }

        updateStatus("Getting recommendations for: " + selectedTrack.getName());
        getRecommendationsButton.setEnabled(false);

        // Get recommendations in background thread
        SwingWorker<List<Track>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Track> doInBackground() throws Exception {
                return serverConnection.getRecommendations(
                        selectedTrack.getName(),
                        selectedTrack.getArtist()
                );
            }

            @Override
            protected void done() {
                try {
                    List<Track> recommendations = get();
                    displayRecommendations(recommendations);
                    updateStatus("Generated " + recommendations.size() + " recommendations");
                    addToLog(selectedTrack, recommendations);
                } catch (Exception e) {
                    showError("Failed to get recommendations: " + e.getMessage());
                    updateStatus("Recommendation failed");
                } finally {
                    getRecommendationsButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    /**
     * Display recommendations in the list
     */
    private void displayRecommendations(List<Track> tracks) {
        recommendationsModel.clear();
        for (Track track : tracks) {
            recommendationsModel.addElement(track);
        }
    }

    /**
     * Update status label
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Show error dialog
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Cleanup resources
     */
    private void cleanup() {
        if (serverConnection != null) {
            serverConnection.disconnect();
        }
    }

    //method writes to the history log
    /**
     * Append this recommendation search to a history text file
     */
    private void addToLog(Track selectedTrack, List<Track> recommendations) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            //print searches to file
            pw.println("Search: " + selectedTrack.getName() + " - " + selectedTrack.getArtist());
            for (Track track : recommendations) {
                pw.println("    " + track.getName() + " - " + track.getArtist());
            }
            pw.println(); // blank line between entries

        } catch (IOException e) {
            // You can also use showError if you want to inform the user
            e.printStackTrace();
        }
    }

    private void displayLog() {
        File file = new File("requestLog.txt");
        DefaultListModel<String> historyModel = new DefaultListModel<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                historyModel.addElement(line);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error reading requestLog.txt: " + e.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        //History log gui creation
        JList<String> historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        // Dialog pane for the log
        JDialog dialog = new JDialog(this, "Search Log History", true);
        dialog.getContentPane().add(scrollPane);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and show GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MusicRecommenderGUI gui = new MusicRecommenderGUI();
            gui.setVisible(true);
        });
    }
}