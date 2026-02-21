import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common'; 
import { Song } from '../../core/services/song/song'; 
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './favorites.html',  //  Matched to your CLI output
  styleUrl: './favorites.css'       //  Matched to your CLI output
})
export class Favorites implements OnInit {
  private songService = inject(Song);
  private router = inject(Router);

  favoriteSongs: any[] = []; 
  currentSong: any = null;

  ngOnInit() {
    this.fetchFavorites();
  }

  fetchFavorites() {
    this.songService.getLikedSongs().subscribe({
      next: (data: any[]) => this.favoriteSongs = data,
      error: (err: any) => console.error('Failed to load favorites:', err)
    });
  }

  // If they click the heart on this page, it "unlikes" it and removes it from the screen
  unlikeSong(event: Event, songId: number) {
    event.stopPropagation(); 
    this.songService.toggleLike(songId).subscribe({
      next: () => {
        // Filter out the song they just unliked so it disappears instantly
        this.favoriteSongs = this.favoriteSongs.filter((song: any) => song.songId !== songId);
      },
      error: (err: any) => console.error('Failed to unlike song:', err)
    });
  }

  playSong(song: any) {
    this.currentSong = song;
  }

  getAudioUrl(fileName: string): string {
    return `${environment.apiUrl}/songs/play/${fileName}`;
  }

  getCoverImageUrl(fileName: string | null): string {
    if (!fileName) return 'assets/default-cover.jpg'; 
    return `${environment.apiUrl}/songs/image/${fileName}`;
  }
}