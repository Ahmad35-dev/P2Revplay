import { Injectable, inject } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../auth/auth'; 

export interface PlayerState {
  song: any;
  autoplay: boolean;
  queue: any[];
  originalQueue: any[]; // Keeps track of the original order for un-shuffling
  currentIndex: number;
  isShuffle: boolean;
  repeatMode: 'off' | 'all' | 'one';
}

@Injectable({
  providedIn: 'root'
})
export class AudioService {
  private authService = inject(AuthService); 
  
  private playerStateSubject = new BehaviorSubject<PlayerState | null>(null);
  playerState$ = this.playerStateSubject.asObservable();

  private getUniqueUserId(): string {
    const token = this.authService.getToken();
    if (!token) return 'unknown_user';
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload);
      const parsed = JSON.parse(decoded);
      return parsed.sub || parsed.email || 'unknown_user'; 
    } catch (e) {
      return 'unknown_user';
    }
  }

  playSong(song: any, queue: any[] = []) {
    const currentState = this.playerStateSubject.getValue();
    const isShuffle = currentState ? currentState.isShuffle : false;
    const repeatMode = currentState ? currentState.repeatMode : 'off';
    
    let originalQueue = queue.length > 0 ? [...queue] : currentState?.originalQueue || [song];
    let currentQueue = [...originalQueue];

    if (isShuffle) {
      // Shuffle the queue, but put the selected song first
      const otherSongs = currentQueue.filter(s => s.songId !== song.songId);
      for (let i = otherSongs.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [otherSongs[i], otherSongs[j]] = [otherSongs[j], otherSongs[i]];
      }
      currentQueue = [song, ...otherSongs];
    }

    let index = currentQueue.findIndex(s => s.songId === song.songId);
    if (index === -1) {
      currentQueue = [song];
      originalQueue = [song];
      index = 0;
    }

    const newState: PlayerState = { 
      song, autoplay: true, queue: currentQueue, originalQueue, currentIndex: index, isShuffle, repeatMode
    };
    this.playerStateSubject.next(newState);

    const userId = this.getUniqueUserId();
    if (userId !== 'unknown_user') {
      localStorage.setItem(`savedState_${userId}`, JSON.stringify(newState));
    }
  }

  playNext(manualSkip: boolean = false) {
    const state = this.playerStateSubject.getValue();
    if (!state || !state.queue || state.queue.length === 0) return;

    // If auto-advancing and repeat ONE is on, just play the same song again
    if (!manualSkip && state.repeatMode === 'one') {
      this.playSong(state.song, state.originalQueue);
      return;
    }

    let nextIndex = state.currentIndex + 1;
    
    if (nextIndex >= state.queue.length) {
      if (state.repeatMode === 'off' && !manualSkip) {
        // End of queue, don't repeat
        return;
      }
      nextIndex = 0; // Loop back
    }

    this.playSong(state.queue[nextIndex], state.originalQueue);
  }

  playPrevious() {
    const state = this.playerStateSubject.getValue();
    if (!state || !state.queue || state.queue.length === 0) return;

    let prevIndex = state.currentIndex - 1;
    if (prevIndex < 0) {
      prevIndex = state.queue.length - 1;
    }

    this.playSong(state.queue[prevIndex], state.originalQueue);
  }

  toggleShuffle() {
    const state = this.playerStateSubject.getValue();
    if (!state) return;

    const newShuffleState = !state.isShuffle;
    let newQueue = [...state.originalQueue];
    let newIndex = 0;

    if (newShuffleState) {
      const otherSongs = newQueue.filter(s => s.songId !== state.song.songId);
      for (let i = otherSongs.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [otherSongs[i], otherSongs[j]] = [otherSongs[j], otherSongs[i]];
      }
      newQueue = [state.song, ...otherSongs];
    } else {
      newIndex = newQueue.findIndex(s => s.songId === state.song.songId);
    }

    this.playerStateSubject.next({
      ...state,
      isShuffle: newShuffleState,
      queue: newQueue,
      currentIndex: newIndex
    });
  }

  toggleRepeat() {
    const state = this.playerStateSubject.getValue();
    if (!state) return;

    let nextMode: 'off' | 'all' | 'one' = 'off';
    if (state.repeatMode === 'off') nextMode = 'all';
    else if (state.repeatMode === 'all') nextMode = 'one';

    this.playerStateSubject.next({ ...state, repeatMode: nextMode });
  }

  restoreUserSong() {
    if (!this.playerStateSubject.getValue()) {
      const userId = this.getUniqueUserId();
      const savedState = localStorage.getItem(`savedState_${userId}`);
      
      if (savedState) {
        try {
          const parsedState = JSON.parse(savedState);
          parsedState.autoplay = false; 
          this.playerStateSubject.next(parsedState);
        } catch (error) {
          console.error('Failed to parse saved state', error);
        }
      }
    }
  }

  clearSong() {
    this.playerStateSubject.next(null);
  }

  getAudioUrl(fileName: string): string {
    if (!fileName) return '';
    return `${environment.apiUrl}/songs/play/${fileName}`;
  }

  getCoverImageUrl(fileName: string | null): string {
    if (!fileName) return 'assets/default-cover.jpg';
    return `${environment.apiUrl}/songs/image/${fileName}`;
  }
}