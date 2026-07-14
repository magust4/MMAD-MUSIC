import { Item } from "../item/item.type";

export interface Review {
  id: number;
  rating: number;
  description: string;
  item: Item;
  username: string;
  createdAt: string;
  updatedAt: string;

  likeCount: number;
  likedByCurrentUser: boolean;
}