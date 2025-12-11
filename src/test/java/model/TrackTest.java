package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

//Tests check track functionality
class TrackTest {

    private Track track;

    @BeforeEach
    void setUp() {
        //Initializing setup artist and song
        track = new Track("Fireflies", "Owl City");
    }
    //Tests below
    @Test
    @DisplayName("Constructor should initialize name and artist")
    void testConstructor() {
        assertEquals("Fireflies", track.getName());
        assertEquals("Owl City", track.getArtist());
    }
    @Test
    @DisplayName("Setters functionality tests")
    void testSetters() {
        track.setUrl("https://www.last.fm/music/Owl+City/_/Fireflies");
        track.setListeners(5000000);
        track.setMatchScore(0.95);
        track.setImageUrl("https://example.com/image.jpg");
        track.setMbid("abc123");

        assertEquals("https://www.last.fm/music/Owl+City/_/Fireflies", track.getUrl());
        assertEquals(5000000, track.getListeners());
        assertEquals(0.95, track.getMatchScore(), 0.001);
        assertEquals("https://example.com/image.jpg", track.getImageUrl());
        assertEquals("abc123", track.getMbid());
    }
    @Test
    @DisplayName("match string below with track-song format")
    void testToStringWithoutMatchScore() {
        String result = track.toString();
        assertEquals("Owl City - Fireflies", result);
    }
    @Test
    @DisplayName("Match output artist, song name, match score")
    void testToStringWithMatchScore() {
        track.setMatchScore(0.87);
        String result = track.toString();
        assertEquals("Owl City - Fireflies (Match: 0.87)", result);
    }
    @Test
    @DisplayName("Default values set to null or zero")
    void testDefaultValues() {
        assertNull(track.getUrl());
        assertNull(track.getMbid());
        assertNull(track.getImageUrl());
        assertEquals(0, track.getListeners());
        assertEquals(0.0, track.getMatchScore(), 0.001);
    }
    @Test
    @DisplayName("handle case where artist and track are null")
    void testNullValues() {
        Track nullTrack = new Track(null, null);
        assertNull(nullTrack.getName());
        assertNull(nullTrack.getArtist());
    }

    @Test
    @DisplayName("should not be equal")
    void testInequality() {
        Track differentTrack = new Track("Vanilla Twilight", "Owl City");
        assertNotEquals(track, differentTrack);
    }
    @Test
    @DisplayName("Make sure track.setListeners can handle large vals")
    void testLargeListenerCount() {
        track.setListeners(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, track.getListeners());
    }
}