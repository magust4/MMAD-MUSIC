import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { ReviewViewerComponent } from '../../review/review-viewer/review-viewer.component';
import { ItemCardComponent } from '../../item/item.component';
import { AlbumSongsComponent } from '../../item/album-songs/album-songs/album-songs.component';
import { ItemPageSkeletonComponent } from './item-page-skeleton/item-page-skeleton.component';

import { ItemReviewViewModel } from '../../../core/model/review/ItemReviewsResponse';
import { PageService } from '../../../service/page/page.service';
import { UiService } from '../../../service/ui/ui.service';
import { ReviewStateService } from '../../../service/review/review-state/review-state.service.spec';

import { Item } from '../../../core/model/item/item.type';
import { Album } from '../../../core/model/item/album.type';
import { Artist } from '../../../core/model/item/artist.type';
import { Song } from '../../../core/model/item/song.type';

import { SimplifiedSong } from '../../../core/model/page/item-page.type';

@Component({
  selector: 'app-item-page',
  standalone: true,
  imports: [
    CommonModule,
    ReviewViewerComponent,
    ItemCardComponent,
    AlbumSongsComponent,
    ItemPageSkeletonComponent
  ],
  templateUrl: './item-page.component.html',
  styleUrls: ['./item-page.component.css']
})
export class ItemPageComponent implements OnInit, OnDestroy {

  item: Item | null = null;

  reviews: ItemReviewViewModel[] = [];

  songs: SimplifiedSong[] = [];
  albums: Album[] = [];

  albumDurationMs: number | null = null;

  averageRating = 0;
  reviewCount = 0;

  itemId: number | null = null;

  cardComponent = 'ItemReviewCardComponent';

  isLoading = true;
  errorMessage = '';

  private reviewSub?: Subscription;


  constructor(
    private route: ActivatedRoute,
    private pageService: PageService,
    private ui: UiService,
    private router: Router,
    private reviewStateService: ReviewStateService
  ) { }


  ngOnInit(): void {

    this.route.params.subscribe(params => {

      const id = Number(params['id']);

      if (!id) {
        return;
      }

      this.itemId = id;

      this.loadPage(id);

    });


    this.reviewSub =
      this.reviewStateService.reviewCreated$
        .subscribe(review => {

          if (
            review &&
            this.itemId &&
            review.item.id === this.itemId
          ) {

            this.reviews = [
              review,
              ...this.reviews
            ];

            this.reviewCount++;

          }

        });

  }


  ngOnDestroy(): void {

    this.reviewSub?.unsubscribe();

  }


  loadPage(id: number): void {

    this.isLoading = true;
    this.errorMessage = '';

    this.pageService.getItemPage(id).subscribe({

      next: (res) => {

        this.item = res.item;

        this.reviews = res.reviews ?? [];

        this.songs = res.songs ?? [];

        this.albums = res.albums ?? [];

        this.albumDurationMs = res.albumDurationMs ?? null;

        this.averageRating = res.averageRating ?? 0;

        this.reviewCount = res.reviewCount ?? 0;

        this.isLoading = false;

      },

      error: (err) => {

        console.error(err);

        this.errorMessage = 'Failed to load page';

        this.item = null;

        this.reviews = [];

        this.songs = [];

        this.albums = [];

        this.albumDurationMs = null;

        this.averageRating = 0;

        this.reviewCount = 0;

        this.isLoading = false;

      }

    });

  }


  openReviewModal(): void {

    if (!this.item) {
      return;
    }

    this.ui.openReviewBuilder(this.item);

  }


  openArtist(artist: Artist): void {

    if (!artist.id) {
      return;
    }

    this.router.navigate([
      '/item',
      artist.id
    ]);

  }


  get album(): Album | null {

    return this.item?.type === 'album'
      ? this.item as Album
      : null;

  }


  get artist(): Artist | null {

    return this.item?.type === 'artist'
      ? this.item as Artist
      : null;

  }


  get song(): Song | null {

    return this.item?.type === 'song'
      ? this.item as Song
      : null;

  }


  get isAlbum(): boolean {

    return this.item?.type === 'album';

  }


  get isArtist(): boolean {

    return this.item?.type === 'artist';

  }


  get isSong(): boolean {

    return this.item?.type === 'song';

  }


  formatDuration(durationMs: number | null | undefined): string {

    if (!durationMs) {
      return '';
    }

    const totalSeconds = Math.floor(durationMs / 1000);

    const minutes = Math.floor(totalSeconds / 60);

    const seconds = totalSeconds % 60;

    return `${minutes}:${seconds.toString().padStart(2, '0')}`;

  }

}