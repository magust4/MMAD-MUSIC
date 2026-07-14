import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import { ProfileViewerComponent } from '../profile-viewer/profile-viewer.component';

import { ReviewService } from '../../../../service/review/review.service';
import { UserService, UserDTO } from '../../../../service/user/user.service';
import { AuthService } from '../../../../service/user/auth/auth.service';
import { ReviewStateService } from '../../../../service/review/review-state/review-state.service.spec';

import { Review } from '../../../../core/model/review/review.type';

import { MatDialog } from '@angular/material/dialog';
import { UserListDialogComponent } from '../../../user-list-dialog/user-list-dialog.component';

import { Router } from '@angular/router';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [
    CommonModule,
    ProfileViewerComponent
  ],
  templateUrl: './profile-page.component.html'
})
export class ProfilePageComponent implements OnInit, OnDestroy {

  currentUser: UserDTO | null = null;

  reviews: Review[] = [];

  isLoading = true;

  isReviewsLoading = true;

  errorMessage = '';

  cardComponent = 'ReviewCardComponent';

  private reviewSub?: Subscription;


  constructor(
    private userService: UserService,
    private reviewService: ReviewService,
    private reviewStateService: ReviewStateService,
    private authService: AuthService,
    private router: Router,
    private dialog: MatDialog
  ) { }


  ngOnInit(): void {

    this.loadProfile();


    this.reviewSub =
      this.reviewStateService.reviewCreated$
        .subscribe(review => {

          if (
            review &&
            this.currentUser?.username === review.username
          ) {

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


  loadProfile(): void {

    this.isLoading = true;

    this.userService.getMyProfile()
      .subscribe({

        next: (user) => {

          this.currentUser = user;

          this.isLoading = false;

          if (user.username) {

            this.loadReviews(user.username);

          } else {

            this.isReviewsLoading = false;

          }

        },

        error: () => {

          this.errorMessage = 'Failed to load profile';

          this.isLoading = false;

          this.isReviewsLoading = false;

        }

      });

  }


  loadReviews(username: string): void {

    this.isReviewsLoading = true;

    this.reviewService.getUserReviews(username)
      .subscribe({

        next: (reviews) => {

          this.reviews = reviews ?? [];

          this.isReviewsLoading = false;

        },

        error: () => {

          this.errorMessage = 'Failed to load reviews';

          this.reviews = [];

          this.isReviewsLoading = false;

        }

      });

  }


  refreshReviews(): void {

    if (!this.currentUser?.username) {
      return;
    }

    this.loadReviews(this.currentUser.username);

  }


  logout(): void {

    this.authService.logout();

    this.router.navigateByUrl('/login');

  }


  openUserList(data: {
    title: string,
    users: string[]
  }): void {

    this.dialog.open(UserListDialogComponent, {

      width: '400px',

      data

    });

  }

}