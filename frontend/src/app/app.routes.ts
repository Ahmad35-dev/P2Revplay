import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { Home } from './features/home/home';
import { UploadSong } from './features/music/upload-song/upload-song'; 
import { Favorites } from './features/favorites/favorites'; 
import { MyMusic } from './features/music/my-music/my-music'; //  NEW: Import the MyMusic class
import { authGuard } from './core/guards/auth/auth-guard';  
import { artistGuard } from './core/guards/artist/artist-guard';  

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  
  // Protected by authGuard (must be logged in)
  { path: 'home', component: Home, canActivate: [authGuard] }, 
  { path: 'favorites', component: Favorites, canActivate: [authGuard] }, 
  
  // Protected by artistGuard (must be logged in AND an ARTIST)
  { path: 'upload', component: UploadSong, canActivate: [artistGuard] },
  
  //  NEW: Protected My Music route for artists
  { path: 'my-music', component: MyMusic, canActivate: [artistGuard] },
  
  { path: '', redirectTo: '/login', pathMatch: 'full' } 
];