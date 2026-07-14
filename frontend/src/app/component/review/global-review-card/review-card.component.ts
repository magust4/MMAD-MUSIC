import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TimeAgoPipe } from '../../../core/pipe/time-ago.pipe';

import { Review } from '../../../core/model/review/review.type';
import { Artist } from '../../../core/model/item/artist.type';
import { Album } from '../../../core/model/item/album.type';
import { Song } from '../../../core/model/item/song.type';
import { Item } from '../../../core/model/item/item.type';

import { ReviewService } from '../../../service/review/review.service';

@Component({
  selector: 'app-review-card',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    TimeAgoPipe
  ],
  templateUrl: './review-card.component.html',
  styleUrl: './review-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewCardComponent implements OnChanges {

  @Input() review!: Review;


  constructor(
    private reviewService: ReviewService,
    private cdr: ChangeDetectorRef
  ) { }


  ngOnChanges() {

    console.log(
      "Review state:",
      this.review.id,
      this.review.likedByCurrentUser,
      this.review.likeCount
    );

  }



  // -------------------------
  // TYPE GUARDS
  // -------------------------

  isArtist(item: Item): item is Artist {
    return item.type === 'artist';
  }


  isAlbum(item: Item): item is Album {
    return item.type === 'album';
  }


  isSong(item: Item): item is Song {
    return item.type === 'song';
  }



  // -------------------------
  // GETTERS
  // -------------------------

  get item(): Item | null {
    return this.review?.item ?? null;
  }


  get itemType(): string {
    return this.item?.type ?? '';
  }


  get itemName(): string {
    return this.item?.name ?? '';
  }


  get itemImage(): string {
    return this.item?.imageURL ?? '';
  }


  get artists(): Artist[] {

    if (!this.item) {
      return [];
    }

    if (this.isAlbum(this.item) || this.isSong(this.item)) {
      return this.item.artists ?? [];
    }

    return [];
  }


  get artistNames(): string {

    return this.artists
      .map(artist => artist.name)
      .join(', ');
  }


  get artist(): Artist | null {

    return this.item && this.isArtist(this.item)
      ? this.item
      : null;

  }


  get stars(): number[] {
    return Array(this.review?.rating || 0);
  }



  // -------------------------
  // UPDATED CHECK
  // -------------------------

  get wasUpdated(): boolean {

    if (!this.review?.createdAt || !this.review?.updatedAt) {
      return false;
    }

    const created =
      new Date(this.review.createdAt).getTime();

    const updated =
      new Date(this.review.updatedAt).getTime();

    return Math.abs(updated - created) > 1000;
  }



  // -------------------------
  // LIKE
  // -------------------------

  toggleLike(): void {

    console.log(
      "BEFORE CLICK:",
      this.review.likedByCurrentUser,
      this.review.likeCount
    );


    if (this.review.likedByCurrentUser) {

      this.reviewService
        .unlikeReview(this.review.id)
        .subscribe(() => {

          this.review = {
            ...this.review,
            likedByCurrentUser: false,
            likeCount: Math.max(
              0,
              this.review.likeCount - 1
            )
          };

          console.log(
            "AFTER UNLIKE:",
            this.review.likedByCurrentUser,
            this.review.likeCount
          );

          this.cdr.markForCheck();

        });


    } else {

      this.reviewService
        .likeReview(this.review.id)
        .subscribe(() => {

          this.review = {
            ...this.review,
            likedByCurrentUser: true,
            likeCount: this.review.likeCount + 1
          };


          console.log(
            "AFTER LIKE:",
            this.review.likedByCurrentUser,
            this.review.likeCount
          );


          this.cdr.markForCheck();

        });

    }

  }

}