import { Component, OnInit, OnDestroy, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { Item } from '../../../core/model/item/item.type';
import { Review } from '../../../core/model/review/review.type';

import { SearchBarComponent } from '../../search-bar/search-bar.component';
import { ReviewService } from '../../../service/review/review.service';
import { AuthService } from '../../../service/user/auth/auth.service';
import { UiService } from '../../../service/ui/ui.service';
import { ItemPreviewComponent } from '../../item/item-preview/item-preview.component';
import { ReviewStateService } from '../../../service/review/review-state/review-state.service.spec';
@Component({
  selector: 'app-review-builder',
  standalone: true,
  imports: [
    CommonModule,
    SearchBarComponent,
    FormsModule,
    ItemPreviewComponent
  ],
  templateUrl: './review-builder.component.html',
  styleUrl: './review-builder.component.css'
})
export class ReviewBuilderComponent implements OnInit, OnDestroy {

  isOpen = false;

  selectedItem: Item | null = null;

  rating = 0;

  text = '';

  errorMessage = '';

  existingReviewId: number | null = null;

  isEditing = false;

  showUpdatePrompt = false;

  pendingReview: Review | null = null;

  private sub?: Subscription;



  constructor(
    private reviewService: ReviewService,
    private authService: AuthService,
    private ui: UiService,
    private reviewStateService: ReviewStateService
  ) { }



  ngOnInit(): void {

    this.sub = this.ui.reviewOpen$.subscribe(open => {

      this.isOpen = open;


      if (open) {

        this.rating = 0;
        this.text = '';
        this.errorMessage = '';

        this.existingReviewId = null;
        this.isEditing = false;

        this.showUpdatePrompt = false;
        this.pendingReview = null;


        this.selectedItem =
          this.ui.selectedItem ?? null;


        if (this.selectedItem) {

          this.checkExistingReview(
            this.selectedItem
          );

        }

      }

    });

  }



  ngOnDestroy(): void {

    this.sub?.unsubscribe();

  }



  close(): void {

    this.ui.closeReviewBuilder();

    this.reset();

  }



  reset(): void {

    this.selectedItem = null;

    this.rating = 0;

    this.text = '';

    this.errorMessage = '';

    this.existingReviewId = null;

    this.isEditing = false;

    this.showUpdatePrompt = false;

    this.pendingReview = null;

  }



  selectItem(item: Item): void {

    this.selectedItem = item;

    this.checkExistingReview(item);

  }



  changeItem(): void {

    this.selectedItem = null;

    this.rating = 0;

    this.text = '';

    this.existingReviewId = null;

    this.isEditing = false;

    this.showUpdatePrompt = false;

    this.pendingReview = null;

  }



  checkExistingReview(item: Item): void {

    const username =
      this.authService.getUsername();


    if (!username || !item.id) {

      return;

    }


    this.reviewService
      .getReviewByUserAndItem(
        username,
        item.id
      )
      .subscribe({

        next: (review) => {

          this.pendingReview = review;

          this.showUpdatePrompt = true;

        },


        error: (err) => {

          if (err.status === 404) {

            this.pendingReview = null;

            this.showUpdatePrompt = false;

            return;

          }


          console.error(
            'Failed checking existing review',
            err
          );

        }

      });

  }



  acceptUpdate(): void {

    if (!this.pendingReview) {

      return;

    }


    this.existingReviewId =
      this.pendingReview.id;


    this.rating =
      this.pendingReview.rating;


    this.text =
      this.pendingReview.description;


    this.isEditing = true;

    this.showUpdatePrompt = false;

  }



  cancelUpdate(): void {

    this.showUpdatePrompt = false;

    this.pendingReview = null;

    this.changeItem();

  }



  setRating(star: number): void {

    this.rating = star;

  }



  submit(): void {


    if (!this.selectedItem) {

      return;

    }


    const username =
      this.authService.getUsername();


    if (!username) {

      this.errorMessage =
        'You must be logged in to post a review';

      return;

    }



    const payload = {

      itemId: this.selectedItem.id!,

      rating: this.rating,

      description: this.text

    };



    const request$ = this.isEditing &&
      this.existingReviewId

      ? this.reviewService.updateReview(
        this.existingReviewId,
        {
          rating: this.rating,
          description: this.text
        }
      )

      : this.reviewService.createReview(
        payload
      );



    request$.subscribe({

      next: (review: Review) => {


        if (!this.isEditing) {

          this.reviewStateService.addReview(review);

        }


        this.close();

      },


      error: (err) => {


        console.error(
          'Failed to save review:',
          err
        );


        this.errorMessage =
          'Failed to save review';


      }

    });


  }


}