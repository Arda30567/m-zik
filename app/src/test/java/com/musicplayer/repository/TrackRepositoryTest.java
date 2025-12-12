package com.musicplayer.repository;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.musicplayer.data.local.AppDatabase;
import com.musicplayer.data.local.entities.Track;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.TestObserver;

import static org.junit.Assert.*;

/**
 * Unit tests for TrackRepository.
 */
@RunWith(AndroidJUnit4.class)
public class TrackRepositoryTest {
    
    private AppDatabase database;
    private TrackRepository trackRepository;
    
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        
        // Create repository with in-memory database
        trackRepository = TrackRepository.getInstance(context);
    }
    
    @After
    public void tearDown() {
        database.close();
        TrackRepository.destroyInstance();
    }
    
    @Test
    public void testInsertAndRetrieveTrack() {
        // Create a test track
        Track track = new Track("Test Song", "Test Artist", "Test Album", "/path/to/test.mp3");
        track.setDuration(180000); // 3 minutes
        track.setTrackNumber(1);
        track.setYear(2023);
        track.setGenre("Pop");
        
        // Insert track
        TestObserver<Void> insertObserver = new TestObserver<>();
        trackRepository.insert(track).subscribe(insertObserver);
        insertObserver.assertComplete();
        insertObserver.assertNoErrors();
        
        // Retrieve all tracks
        TestObserver<List<Track>> getAllObserver = new TestObserver<>();
        trackRepository.getAll().first(new ArrayList<>()).subscribe(getAllObserver);
        
        List<Track> tracks = getAllObserver.values().get(0);
        assertEquals(1, tracks.size());
        
        Track retrievedTrack = tracks.get(0);
        assertEquals("Test Song", retrievedTrack.getTitle());
        assertEquals("Test Artist", retrievedTrack.getArtist());
        assertEquals("Test Album", retrievedTrack.getAlbum());
        assertEquals("/path/to/test.mp3", retrievedTrack.getFilePath());
        assertEquals(180000, retrievedTrack.getDuration());
        assertEquals(1, retrievedTrack.getTrackNumber());
        assertEquals(2023, retrievedTrack.getYear());
        assertEquals("Pop", retrievedTrack.getGenre());
    }
    
    @Test
    public void testUpdateTrack() {
        // Create and insert a track
        Track track = new Track("Original Title", "Original Artist", "Original Album", "/path/to/test.mp3");
        trackRepository.insert(track).blockingAwait();
        
        // Get the track ID
        List<Track> tracks = trackRepository.getAll().first(new ArrayList<>()).blockingGet();
        long trackId = tracks.get(0).getId();
        
        // Update the track
        TestObserver<Void> updateObserver = new TestObserver<>();
        trackRepository.updateMetadata(trackId, "Updated Title", "Updated Artist", "Updated Album", 
                "Rock", 2024, 2, "Updated lyrics", "Updated composer")
                .subscribe(updateObserver);
        updateObserver.assertComplete();
        updateObserver.assertNoErrors();
        
        // Verify the update
        TestObserver<Track> getByIdObserver = new TestObserver<>();
        trackRepository.getById(trackId).subscribe(getByIdObserver);
        
        Track updatedTrack = getByIdObserver.values().get(0);
        assertEquals("Updated Title", updatedTrack.getTitle());
        assertEquals("Updated Artist", updatedTrack.getArtist());
        assertEquals("Updated Album", updatedTrack.getAlbum());
        assertEquals("Rock", updatedTrack.getGenre());
        assertEquals(2024, updatedTrack.getYear());
        assertEquals(2, updatedTrack.getTrackNumber());
    }
    
    @Test
    public void testDeleteTrack() {
        // Create and insert a track
        Track track = new Track("Test Song", "Test Artist", "Test Album", "/path/to/test.mp3");
        trackRepository.insert(track).blockingAwait();
        
        // Get the track ID
        List<Track> tracks = trackRepository.getAll().first(new ArrayList<>()).blockingGet();
        assertEquals(1, tracks.size());
        
        // Delete the track
        TestObserver<Void> deleteObserver = new TestObserver<>();
        trackRepository.delete(tracks.get(0)).subscribe(deleteObserver);
        deleteObserver.assertComplete();
        deleteObserver.assertNoErrors();
        
        // Verify deletion
        TestObserver<Integer> countObserver = new TestObserver<>();
        trackRepository.getCount().subscribe(countObserver);
        assertEquals(0, countObserver.values().get(0).intValue());
    }
    
    @Test
    public void testToggleFavorite() {
        // Create and insert a track
        Track track = new Track("Test Song", "Test Artist", "Test Album", "/path/to/test.mp3");
        trackRepository.insert(track).blockingAwait();
        
        // Get the track ID
        List<Track> tracks = trackRepository.getAll().first(new ArrayList<>()).blockingGet();
        long trackId = tracks.get(0).getId();
        
        // Initially not favorite
        assertFalse(tracks.get(0).isFavorite());
        
        // Toggle favorite (should become true)
        TestObserver<Void> toggleObserver = new TestObserver<>();
        trackRepository.toggleFavorite(trackId).subscribe(toggleObserver);
        toggleObserver.assertComplete();
        toggleObserver.assertNoErrors();
        
        // Verify favorite status
        TestObserver<Track> getByIdObserver = new TestObserver<>();
        trackRepository.getById(trackId).subscribe(getByIdObserver);
        assertTrue(getByIdObserver.values().get(0).isFavorite());
        
        // Toggle again (should become false)
        trackRepository.toggleFavorite(trackId).blockingAwait();
        Track trackAfterSecondToggle = trackRepository.getById(trackId).blockingGet();
        assertFalse(trackAfterSecondToggle.isFavorite());
    }
    
    @Test
    public void testSearchTracks() {
        // Create and insert multiple tracks
        List<Track> testTracks = new ArrayList<>();
        testTracks.add(new Track("Rock Song", "Rock Band", "Rock Album", "/path/to/rock.mp3"));
        testTracks.add(new Track("Pop Song", "Pop Artist", "Pop Album", "/path/to/pop.mp3"));
        testTracks.add(new Track("Jazz Track", "Jazz Musician", "Jazz Album", "/path/to/jazz.mp3"));
        
        for (Track track : testTracks) {
            trackRepository.insert(track).blockingAwait();
        }
        
        // Search for "Rock"
        TestObserver<List<Track>> searchObserver = new TestObserver<>();
        trackRepository.search("Rock").first(new ArrayList<>()).subscribe(searchObserver);
        
        List<Track> searchResults = searchObserver.values().get(0);
        assertEquals(1, searchResults.size());
        assertEquals("Rock Song", searchResults.get(0).getTitle());
        
        // Search for "Song" (should find Rock Song and Pop Song)
        TestObserver<List<Track>> searchObserver2 = new TestObserver<>();
        trackRepository.search("Song").first(new ArrayList<>()).subscribe(searchObserver2);
        
        List<Track> searchResults2 = searchObserver2.values().get(0);
        assertEquals(2, searchResults2.size());
    }
    
    @Test
    public void testGetFavorites() {
        // Create and insert tracks
        Track track1 = new Track("Song 1", "Artist 1", "Album 1", "/path/to/song1.mp3");
        Track track2 = new Track("Song 2", "Artist 2", "Album 2", "/path/to/song2.mp3");
        track2.setFavorite(true);
        Track track3 = new Track("Song 3", "Artist 3", "Album 3", "/path/to/song3.mp3");
        track3.setFavorite(true);
        
        trackRepository.insert(track1).blockingAwait();
        trackRepository.insert(track2).blockingAwait();
        trackRepository.insert(track3).blockingAwait();
        
        // Get favorites
        TestObserver<List<Track>> favoritesObserver = new TestObserver<>();
        trackRepository.getFavorites().first(new ArrayList<>()).subscribe(favoritesObserver);
        
        List<Track> favorites = favoritesObserver.values().get(0);
        assertEquals(2, favorites.size());
    }
    
    @Test
    public void testIncrementPlayCount() {
        // Create and insert a track
        Track track = new Track("Test Song", "Test Artist", "Test Album", "/path/to/test.mp3");
        track.setPlayCount(5);
        trackRepository.insert(track).blockingAwait();
        
        // Get the track ID
        List<Track> tracks = trackRepository.getAll().first(new ArrayList<>()).blockingGet();
        long trackId = tracks.get(0).getId();
        
        // Increment play count
        TestObserver<Void> incrementObserver = new TestObserver<>();
        trackRepository.incrementPlayCount(trackId).subscribe(incrementObserver);
        incrementObserver.assertComplete();
        incrementObserver.assertNoErrors();
        
        // Verify play count was incremented
        Track updatedTrack = trackRepository.getById(trackId).blockingGet();
        assertEquals(6, updatedTrack.getPlayCount());
    }
}