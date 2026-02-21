import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 
import { AuthService } from '../../core/services/auth'; 
import { Song } from '../../core/services/song/song'; 
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
  private router = inject(Router);

  userName: string = '';
  userRole: string = '';
  songs: any[] = []; 
  
  currentSong: any = null;

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

  onSearch() {
    if (this.searchQuery.trim() === '') {
      this.fetchSongs(); 
      return;
    }
    this.songService.searchSongsByTitle(this.searchQuery).subscribe({
      next: (data) => this.songs = data,
      error: (err) => console.error('Search failed:', err)
    });
  }

  onFilterChange() {
    if (this.selectedGenre === '') {
      this.fetchSongs(); 
      return;
    }
    this.songService.filterSongsByGenre(this.selectedGenre).subscribe({
      next: (data) => this.songs = data,
      error: (err) => console.error('Filter failed:', err)
    });
  }

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

  // --- NEW: Generate the URL for the cover image ---
  getCoverImageUrl(fileName: string | null): string {
    if (!fileName) {
      return 'assets/default-cover.jpg'; // Fallback just in case
    }
    return `${environment.apiUrl}/songs/image/${fileName}`;
  }

  onLogout() {
    this.authService.logout();       
    this.router.navigate(['/login']); 
  }
}