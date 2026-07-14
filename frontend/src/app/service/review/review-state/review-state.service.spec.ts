import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Review } from '../../../core/model/review/review.type';

@Injectable({
  providedIn: 'root'
})
export class ReviewStateService {

  private reviewCreatedSource =
    new BehaviorSubject<Review | null>(null);


  reviewCreated$ =
    this.reviewCreatedSource.asObservable();



  addReview(review: Review): void {

    this.reviewCreatedSource.next(review);

  }

}