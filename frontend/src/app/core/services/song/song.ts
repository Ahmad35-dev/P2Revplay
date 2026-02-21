import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../auth/auth'; 

@Injectable({
  providedIn: 'root'
})
export class Song {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = `${environment.apiUrl}/songs`;

  getAllSongs(): Observable<any[]> {
    const token = this.authService.getToken(); 
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<any[]>(this.apiUrl, { headers });
  }

  uploadSong(songData: FormData): Observable<any> {
    const token = this.authService.getToken(); 
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.post(this.apiUrl, songData, { headers });
  }

  searchSongsByTitle(title: string): Observable<any[]> {
    const token = this.authService.getToken(); 
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<any[]>(`${this.apiUrl}/search?title=${title}`, { headers });
  }

  filterSongsByGenre(genre: string): Observable<any[]> {
    const token = this.authService.getToken(); 
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<any[]>(`${this.apiUrl}/filter?genre=${genre}`, { headers });
  }

  // --- NEW: Toggle Like on a Song ---
  toggleLike(songId: number): Observable<boolean> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.post<boolean>(`${this.apiUrl}/${songId}/like`, {}, { headers });
  }

  // --- NEW: Get all Liked Songs ---
  getLikedSongs(): Observable<any[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<any[]>(`${this.apiUrl}/liked`, { headers });
  }
}