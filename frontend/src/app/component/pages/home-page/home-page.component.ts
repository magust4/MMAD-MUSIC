import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import { ReviewStateService } from '../../../service/review/review-state/review-state.service.spec';
import { ReviewViewerComponent } from '../../review/review-viewer/review-viewer.component';
import { ReviewService } from '../../../service/review/review.service';
import { Review } from '../../../core/model/review/review.type';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    ReviewViewerComponent
  ],
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent implements OnInit {

  reviews: Review[] = [];

  cardComponent = 'ReviewCardComponent';

  isLoading = true;

  errorMessage = '';

  private reviewSub?: Subscription;

  constructor(
    private reviewService: ReviewService,
    private reviewStateService: ReviewStateService
  ) { }


  ngOnInit(): void {

    this.loadReviews();


    this.reviewSub =
      this.reviewStateService.reviewCreated$
        .subscribe(review => {

          if (review) {

            this.reviews = [
              review,
              ...this.reviews
            ];

          }

        });

  }

  ngOnDestroy(): void {

    this.reviewSub?.unsubscribe();

  }


  loadReviews(): void {

    this.isLoading = true;

    this.errorMessage = '';

    this.reviewService.getFeedReviews()
      .subscribe({

        next: (reviews: Review[]) => {

          this.reviews = reviews ?? [];

          this.isLoading = false;

        },

        error: () => {

          this.errorMessage = 'Failed to load reviews';

          this.reviews = [];

          this.isLoading = false;

        }

      });

  }


  // NEW
  addReview(review: Review): void {

    this.reviews = [
      review,
      ...this.reviews
    ];

  }

}