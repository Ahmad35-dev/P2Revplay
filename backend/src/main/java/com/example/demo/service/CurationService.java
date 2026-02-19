package com.example.demo.service;

import com.example.demo.dto.PlaylistRequest;
import com.example.demo.dto.PlaylistResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CurationService {

    private final FavoriteRepository favoriteRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final FollowedPlaylistRepository followedPlaylistRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    public CurationService(FavoriteRepository favoriteRepository, PlaylistRepository playlistRepository,
                           PlaylistSongRepository playlistSongRepository, FollowedPlaylistRepository followedPlaylistRepository,
                           UserRepository userRepository, SongRepository songRepository) {
        this.favoriteRepository = favoriteRepository;
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.followedPlaylistRepository = followedPlaylistRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
    }

    // --- 1. FAVORITES LOGIC ---
    @Transactional
    public String toggleFavorite(String email, Long songId) {
        User user = getUser(email);
        Song song = getSong(songId);
        Optional<Favorite> existing = favoriteRepository.findByUserAndSong(user, song);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return "Song removed from favorites.";
        } else {
            Favorite favorite = new Favorite();

            // Handled automatically by @IdClass
            favorite.setUser(user);
            favorite.setSong(song);

            favoriteRepository.save(favorite);
            return "Song added to favorites!";
        }
    }

    // --- 2. PLAYLIST CRUD LOGIC ---
    @Transactional
    public PlaylistResponse createPlaylist(String email, PlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setUser(getUser(email));
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setPrivacy(request.getPrivacy() != null ? request.getPrivacy() : "PUBLIC");
        return mapToResponse(playlistRepository.save(playlist));
    }

    @Transactional
    public PlaylistResponse updatePlaylist(String email, Long playlistId, PlaylistRequest request) {
        Playlist playlist = getPlaylistAsOwner(email, playlistId);
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setPrivacy(request.getPrivacy());
        return mapToResponse(playlistRepository.save(playlist));
    }

    @Transactional
    public String deletePlaylist(String email, Long playlistId) {
        Playlist playlist = getPlaylistAsOwner(email, playlistId);
        playlistRepository.delete(playlist);
        return "Playlist deleted successfully.";
    }

    public List<PlaylistResponse> getMyPlaylists(String email) {
        return playlistRepository.findByUser(getUser(email))
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<PlaylistResponse> getPublicPlaylists() {
        return playlistRepository.findByPrivacy("PUBLIC")
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // --- 3. PLAYLIST SONGS LOGIC ---
    @Transactional
    public String addSongToPlaylist(String email, Long playlistId, Long songId) {
        Playlist playlist = getPlaylistAsOwner(email, playlistId);
        Song song = getSong(songId);

        if (playlistSongRepository.findByPlaylistAndSong(playlist, song).isPresent()) {
            return "Song is already in this playlist.";
        }

        PlaylistSong ps = new PlaylistSong();

        // FIX: Handled automatically by @IdClass. No need for manual ID generation.
        ps.setPlaylist(playlist);
        ps.setSong(song);

        playlistSongRepository.save(ps);
        return "Song added to playlist!";
    }

    @Transactional
    public String removeSongFromPlaylist(String email, Long playlistId, Long songId) {
        Playlist playlist = getPlaylistAsOwner(email, playlistId);
        Song song = getSong(songId);

        PlaylistSong ps = playlistSongRepository.findByPlaylistAndSong(playlist, song)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found in this playlist"));

        playlistSongRepository.delete(ps);
        return "Song removed from playlist.";
    }

    // --- 4. FOLLOW PLAYLIST LOGIC ---
    @Transactional
    public String toggleFollowPlaylist(String email, Long playlistId) {
        User user = getUser(email);
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        if ("PRIVATE".equals(playlist.getPrivacy()) && !playlist.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Cannot follow a private playlist.");
        }

        Optional<FollowedPlaylist> existing = followedPlaylistRepository.findByUserAndPlaylist(user, playlist);

        if (existing.isPresent()) {
            followedPlaylistRepository.delete(existing.get());
            return "Unfollowed playlist.";
        } else {
            FollowedPlaylist follow = new FollowedPlaylist();

            // FIX: Handled automatically by @IdClass
            follow.setUser(user);
            follow.setPlaylist(playlist);

            followedPlaylistRepository.save(follow);
            return "Successfully followed playlist!";
        }
    }

    // --- HELPER METHODS ---
    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Song getSong(Long songId) {
        return songRepository.findById(songId).orElseThrow(() -> new ResourceNotFoundException("Song not found"));
    }

    private Playlist getPlaylistAsOwner(String email, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));
        if (!playlist.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized: You do not own this playlist");
        }
        return playlist;
    }

    private PlaylistResponse mapToResponse(Playlist playlist) {
        PlaylistResponse response = new PlaylistResponse();

        // FIX: Changed to getPlaylistId() to match your new Playlist entity
        response.setPlaylistId(playlist.getPlaylistId());

        response.setName(playlist.getName());
        response.setDescription(playlist.getDescription());
        response.setPrivacy(playlist.getPrivacy());
        response.setCreatorName(playlist.getUser().getName());
        return response;
    }
}