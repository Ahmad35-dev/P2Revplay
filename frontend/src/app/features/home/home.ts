import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; // 👈 NEW: Required for the search input
import { AuthService } from '../../core/services/auth'; 
import { Song } from '../../core/services/song/song'; 
import { environment } from '../../../environments/environment'; 

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule], // 👈 NEW: Added FormsModule
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  private authService = inject(AuthService);
  private songService = inject(Song);
  private router = inject(Router);

  userName: string = '';
  userRole: string = '';
  songs: any[] = []; 
  
  currentSong: any = null;

  // --- NEW: Search and Filter Variables ---
  searchQuery: string = '';
  selectedGenre: string = '';

  ngOnInit() {
    const storedName = this.authService.getUserName();
    this.userName = (storedName && storedName !== 'null') ? storedName : 'User';
    this.userRole = this.authService.getRole() || 'USER';
    this.fetchSongs();
  }

  fetchSongs() {
    this.songService.getAllSongs().subscribe({
      next: (data) => {
        this.songs = data;
        console.log('Successfully loaded songs:', this.songs);
      },
      error: (err) => console.error('Failed to load songs:', err)
    });
  }

  // --- NEW: Search Logic ---
  onSearch() {
    if (this.searchQuery.trim() === '') {
      this.fetchSongs(); // If empty, load all songs
      return;
    }
    this.songService.searchSongsByTitle(this.searchQuery).subscribe({
      next: (data) => this.songs = data,
      error: (err) => console.error('Search failed:', err)
    });
  }

  // --- NEW: Filter Logic ---
  onFilterChange() {
    if (this.selectedGenre === '') {
      this.fetchSongs(); // If "All Genres" is selected, load all
      return;
    }
    this.songService.filterSongsByGenre(this.selectedGenre).subscribe({
      next: (data) => this.songs = data,
      error: (err) => console.error('Filter failed:', err)
    });
  }

  // --- NEW: Clear Filters ---
  clearFilters() {
    this.searchQuery = '';
    this.selectedGenre = '';
    this.fetchSongs();
  }

  playSong(song: any) {
    console.log('Playing:', song.title);
    this.currentSong = song;
  }

  getAudioUrl(fileName: string): string {
    return `${environment.apiUrl}/songs/play/${fileName}`;
  }

  onLogout() {
    this.authService.logout();       
    this.router.navigate(['/login']); 
  }
}