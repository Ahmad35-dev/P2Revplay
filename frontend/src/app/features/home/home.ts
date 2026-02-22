import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 
import { AuthService } from '../../core/services/auth'; 
import { Song } from '../../core/services/song/song'; 
import { Playlist } from '../../core/services/playlist/playlist'; 
import { History } from '../../core/services/history/history'; 
import { AudioService } from '../../core/services/audio/audio'; 
import { environment } from '../../../environments/environment'; 

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule], 
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  private authService = inject(AuthService);
  private songService = inject(Song);
  private playlistService = inject(Playlist); 
  private historyService = inject(History); 
  private audioService = inject(AudioService); 
  private router = inject(Router);

  userName: string = '';
  userRole: string = '';
  songs: any[] = []; 
  
  searchQuery: string = '';
  selectedGenre: string = '';

  likedSongIds: Set<number> = new Set<number>();

  myPlaylists: any[] = [];
  showPlaylistModal: boolean = false;
  selectedSongForPlaylist: any = null;

  recentHistory: any[] = [];

  ngOnInit() {
    const storedName = this.authService.getUserName();
    this.userName = (storedName && storedName !== 'null') ? storedName : 'User';
    this.userRole = this.authService.getRole() || 'USER';
    
    this.fetchSongs();
    this.fetchLikedSongs();
    this.fetchRecentHistory(); 

    this.audioService.restoreUserSong();
  }

  fetchSongs() {
    this.songService.getAllSongs().subscribe({
      next: (data: any[]) => this.songs = data,
      error: (err: any) => console.error('Failed to load songs:', err)
    });
  }

  fetchLikedSongs() {
    this.songService.getLikedSongs().subscribe({
      next: (data: any[]) => this.likedSongIds = new Set(data.map((song: any) => song.songId)),
      error: (err: any) => console.error('Failed to load liked songs:', err)
    });
  }

  fetchRecentHistory() {
    this.historyService.getRecentHistory().subscribe({
      next: (data: any[]) => this.recentHistory = data,
      error: (err: any) => console.error('Failed to load history:', err)
    });
  }

  toggleLike(event: Event, songId: number) {
    event.stopPropagation(); 
    this.songService.toggleLike(songId).subscribe({
      next: (isLiked: boolean) => isLiked ? this.likedSongIds.add(songId) : this.likedSongIds.delete(songId),
      error: (err: any) => console.error('Failed to toggle like:', err)
    });
  }

  isLiked(songId: number): boolean {
    return this.likedSongIds.has(songId);
  }

  onSearch() {
    if (this.searchQuery.trim() === '') return this.fetchSongs(); 
    this.songService.searchSongsByTitle(this.searchQuery).subscribe({
      next: (data: any[]) => this.songs = data,
      error: (err: any) => console.error('Search failed:', err)
    });
  }

  onFilterChange() {
    if (this.selectedGenre === '') return this.fetchSongs(); 
    this.songService.filterSongsByGenre(this.selectedGenre).subscribe({
      next: (data: any[]) => this.songs = data,
      error: (err: any) => console.error('Filter failed:', err)
    });
  }

  clearFilters() {
    this.searchQuery = '';
    this.selectedGenre = '';
    this.fetchSongs();
  }

  playSong(song: any) {
    // NEW: Determine which list they clicked from to set the correct queue
    let currentQueue = this.songs;
    
    // If the song isn't in the main list, they must have clicked 'Recently Played'
    if (!this.songs.some(s => s.songId === song.songId)) {
      currentQueue = this.recentHistory;
    }

    // Map properties so the player gets a consistent object structure
    const songToPlay = {
      songId: song.songId,
      title: song.title || song.songTitle,
      artistName: song.artistName,
      audioFileUrl: song.audioFileUrl || null,
      coverImageUrl: song.coverImageUrl || null 
    };

    // Replace the clicked song object in the queue with this mapped one so findIndex matches
    const mappedQueue = currentQueue.map(s => ({
      songId: s.songId,
      title: s.title || s.songTitle,
      artistName: s.artistName,
      audioFileUrl: s.audioFileUrl || null,
      coverImageUrl: s.coverImageUrl || null 
    }));

    // Send to Global Player!
    this.audioService.playSong(songToPlay, mappedQueue);

    if (song.playCount !== undefined) {
      this.songService.incrementPlayCount(song.songId).subscribe({
        next: () => song.playCount = (song.playCount || 0) + 1,
        error: (err: any) => console.error('Failed to update play count:', err)
      });
    }

    if (song.songId) {
      this.historyService.logPlay(song.songId).subscribe({
        next: () => this.fetchRecentHistory(), 
        error: (err: any) => console.error('Failed to log history:', err)
      });
    }
  }

  openPlaylistModal(event: Event, song: any) {
    event.stopPropagation(); 
    this.selectedSongForPlaylist = song;
    this.playlistService.getMyPlaylists().subscribe({
      next: (data: any[]) => {
        this.myPlaylists = data;
        this.showPlaylistModal = true;
      },
      error: (err: any) => console.error('Failed to load playlists', err)
    });
  }

  closePlaylistModal() {
    this.showPlaylistModal = false;
    this.selectedSongForPlaylist = null;
  }

  addToPlaylist(playlistId: number) {
    if (!this.selectedSongForPlaylist) return;
    this.playlistService.addSongToPlaylist(playlistId, this.selectedSongForPlaylist.songId).subscribe({
      next: (response: string) => {
        alert(response || "Song added to playlist!");
        this.closePlaylistModal();
      },
      error: (err: any) => alert("Failed to add song. It might already be in this playlist.")
    });
  }

  getCoverImageUrl(fileName: string | null): string {
    return fileName ? `${environment.apiUrl}/songs/image/${fileName}` : 'assets/default-cover.jpg';
  }

  onLogout() {
    this.audioService.clearSong();
    this.authService.logout();       
    this.router.navigate(['/login']); 
  }
}