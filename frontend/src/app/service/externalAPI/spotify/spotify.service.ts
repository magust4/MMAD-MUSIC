import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Artist } from '../../../core/model/item/artist.type';
import { Item } from '../../../core/model/item/item.type';
import { Album } from '../../../core/model/item/album.type';
import { Song } from '../../../core/model/item/song.type';
import { ExternalAPIService } from '../external-api.service';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SpotifyService extends ExternalAPIService {
  protected override apiUrl = `${environment.apiUrl}/spotify`;

  constructor(protected override http: HttpClient) {
    super(http);
  }

  override searchItem(itemName: string): Observable<Item[]> {
    return this.http.get<Item[]>(`${this.apiUrl}/search/item/${itemName}`);
  }

  getArtist(sourceId: string): Observable<Artist> {
    return this.http.get<Artist>(`${this.apiUrl}/artist/${sourceId}`);
  }

  getAlbum(sourceId: string): Observable<Album> {
    return this.http.get<Album>(`${this.apiUrl}/album/${sourceId}`);
  }

  getSong(sourceId: string): Observable<Song> {
    return this.http.get<Song>(`${this.apiUrl}/song/${sourceId}`);
  }


}
