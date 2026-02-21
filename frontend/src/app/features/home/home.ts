import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 
import { AuthService } from '../../core/services/auth'; 
import { Song } from '../../core/services/song/song'; 
import { Playlist } from '../../core/services/playlist/playlist'; 
import { History } from '../../core/services/history/history'; // NEW: Import History Service
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
  private historyService = inject(History); // NEW: Inject History Service
  private router = inject(Router);

  userName: string = '';
  userRole: string = '';
  songs: any[] = []; 
  
  currentSong: any = null;

  searchQuery: string = '';
  selectedGenre: string = '';

  likedSongIds: Set<number> = new Set<number>();

  // Playlist Modal Variables
  myPlaylists: any[] = [];
  showPlaylistModal: boolean = false;
  selectedSongForPlaylist: any = null;

  // NEW: Array to hold recently played songs
  recentHistory: any[] = [];

  ngOnInit() {
    const storedName = this.authService.getUserName();
    this.userName = (storedName && storedName !== 'null') ? storedName : 'User';
    this.userRole = this.authService.getRole() || 'USER';
    this.fetchSongs();
    this.fetchLikedSongs();
    this.fetchRecentHistory(); // NEW: Load history on startup
  }

  fetchSongs() {
    this.songService.getAllSongs().subscribe({
      next: (data: any[]) => {
        this.songs = data;
      },
      error: (err: any) => console.error('Failed to load songs:', err)
    });
  }

  fetchLikedSongs() {
    this.songService.getLikedSongs().subscribe({
      next: (data: any[]) => {
        this.likedSongIds = new Set(data.map((song: any) => song.songId));
      },
      error: (err: any) => console.error('Failed to load liked songs:', err)
    });
  }

  // NEW METHOD: Fetch history from backend
  fetchRecentHistory() {
    this.historyService.getRecentHistory().subscribe({
      next: (data: any[]) => this.recentHistory = data,
      error: (err: any) => console.error('Failed to load history:', err)
    });
  }

  toggleLike(event: Event, songId: number) {
    event.stopPropagation(); 
    
    this.songService.toggleLike(songId).subscribe({
      next: (isLiked: boolean) => {
        if (isLiked) {
          this.likedSongIds.add(songId);
        } else {
          this.likedSongIds.delete(songId);
        }
      },
      error: (err: any) => console.error('Failed to toggle like:', err)
    });
  }

  isLiked(songId: number): boolean {
    return this.likedSongIds.has(songId);
  }

  onSearch() {
    if (this.searchQuery.trim() === '') {
      this.fetchSongs(); 
      return;
    }
    this.songService.searchSongsByTitle(this.searchQuery).subscribe({
      next: (data: any[]) => this.songs = data,
      error: (err: any) => console.error('Search failed:', err)
    });
  }

  onFilterChange() {
    if (this.selectedGenre === '') {
      this.fetchSongs(); 
      return;
    }
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

  // UPDATED: Handle playing AND logging to history!
  playSong(song: any) {
    console.log('Playing:', song.title || song.songTitle);
    
    // Support playing both regular songs and history DTOs
    this.currentSong = {
      songId: song.songId,
      title: song.title || song.songTitle,
      artistName: song.artistName,
      audioFileUrl: song.audioFileUrl || null 
    };

    // Update global play count visually (if it's a regular song card)
    if (song.playCount !== undefined) {
      this.songService.incrementPlayCount(song.songId).subscribe({
        next: () => {
          song.playCount = (song.playCount || 0) + 1;
        },
        error: (err: any) => console.error('Failed to update play count:', err)
      });
    }

    // NEW: Log the play to the history table and refresh the row
    if (song.songId) {
      this.historyService.logPlay(song.songId).subscribe({
        next: () => this.fetchRecentHistory(), // Refresh history row instantly
        error: (err: any) => console.error('Failed to log history:', err)
      });
    }
  }

  // --- PLAYLIST MODAL LOGIC ---

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
      error: (err: any) => {
        alert("Failed to add song (It might already be in this playlist).");
        console.error(err);
      }
    });
  }

  // ----------------------------------------

  getAudioUrl(fileName: string): string {
    if(!fileName) return ''; // Failsafe
    return `${environment.apiUrl}/songs/play/${fileName}`;
  }

  getCoverImageUrl(fileName: string | null): string {
    if (!fileName) {
      return 'assets/default-cover.jpg'; 
    }
    return `${environment.apiUrl}/songs/image/${fileName}`;
  }

  onLogout() {
    this.authService.logout();       
    this.router.navigate(['/login']); 
  }
}