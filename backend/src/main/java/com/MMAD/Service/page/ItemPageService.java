package com.MMAD.Service.page;

import java.util.List;

import org.springframework.stereotype.Service;

import com.MMAD.Service.SpotifyService;
import com.MMAD.Service.Review.ReviewService;
import com.MMAD.Service.item.ItemService;
import com.MMAD.dto.item.AlbumDTO;
import com.MMAD.dto.item.ArtistDTO;
import com.MMAD.dto.item.ItemDTO;
import com.MMAD.dto.page.ItemPageDTO;
import com.MMAD.dto.review.ItemReviewResponse;
import com.MMAD.dto.review.ItemReviewsResponse;

@Service
public class ItemPageService {

    private final ItemService itemService;
    private final SpotifyService spotifyService;
    private final ReviewService reviewService;

    public ItemPageService(
            ItemService itemService,
            SpotifyService spotifyService,
            ReviewService reviewService) {

        this.itemService = itemService;
        this.spotifyService = spotifyService;
        this.reviewService = reviewService;
    }

    public ItemPageDTO getItemPage(Long itemId) {

        // Refreshes item data from Spotify if needed
        ItemDTO item = ItemDTO.fromEntity(
                itemService.getItemWithRefresh(itemId));

        List<ItemPageDTO.SimplifiedSong> songs = null;
        List<AlbumDTO> albums = null;

        Integer albumDurationMs = null;

        if (item instanceof AlbumDTO album) {

            songs = spotifyService.getAlbumTracks(
                    album.getSourceId());

            albumDurationMs = songs.stream()
                    .filter(song -> song.durationMs() != null)
                    .mapToInt(ItemPageDTO.SimplifiedSong::durationMs)
                    .sum();
        }

        if (item instanceof ArtistDTO artist) {

            albums = spotifyService.getArtistAlbums(
                    artist.getSourceId());
        }

        ItemReviewsResponse reviewResponse = reviewService.getReviewsByItemId(itemId);

        List<ItemReviewResponse> reviews = reviewResponse.getReviews();

        Double averageRating = reviews.isEmpty()
                ? null
                : reviews.stream()
                        .mapToDouble(ItemReviewResponse::getRating)
                        .average()
                        .orElseThrow();

        Integer reviewCount = reviews.size();

        return new ItemPageDTO(
                item,
                reviews,
                songs,
                albums,
                albumDurationMs,
                averageRating,
                reviewCount);
    }
}