package view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class MusicRecommenderGUITest {
    private MusicRecommenderGUI gui;
    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() ->{
            gui = new MusicRecommenderGUI();
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        SwingUtilities.invokeAndWait(gui::dispose);
    }

    @Test
    void main() {
    }
}