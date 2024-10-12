package kr.co.ureca.urecaminiproject.service;

import kr.co.ureca.urecaminiproject.data.entity.Movie;
import kr.co.ureca.urecaminiproject.data.entity.Review;
import kr.co.ureca.urecaminiproject.data.entity.User;
import kr.co.ureca.urecaminiproject.data.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public void createReview(User user, Movie movie, float reviewRating, String reviewContent) {

        // 리뷰 등록 처리
        Review review = new Review();
        review.setReviewRating(BigDecimal.valueOf(reviewRating));
        review.setReviewContent(reviewContent);

        review.setReviewCreatedAt(LocalDateTime.now());

        review.setMovie(movie);
        review.setUser(user);

        reviewRepository.save(review);

    }

    public void updateReview(Long reviewId, float reviewRating, String reviewContent) {

        // 리뷰 갱신 처리
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setReviewRating(BigDecimal.valueOf(reviewRating));
        review.setReviewContent(reviewContent);
        reviewRepository.save(review);
    }

    public boolean deleteReview(Long reviewId)
    {
        if (reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
            return !reviewRepository.existsById(reviewId);  // 삭제 후 존재 여부 확인
        } else {
            // 삭제할 리뷰가 존재하지 않는 경우
            return false;
        }
    }

    public Review findReviewById(Long reviewId)
    {
        // 리뷰 아이디에 해당하는 리뷰 정보 반환
        //Optional<Review> review = reviewRepository.findById(reviewId);
        //return review.get();
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
    }

    public long getReviewCountByMovieId(Long movieId) {
        return reviewRepository.countReviewsByMovieId(movieId);
    }

    //@Transactional
    public void addReviewLike(Long reviewId, User user) {

        // 리뷰와 사용자 객체 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.addLike(user);

        reviewRepository.save(review);
    }

    public void cancelReviewLike(Long reviewId, User user) {

        // 리뷰와 사용자 객체 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.removeLike(user);

        reviewRepository.save(review);
    }

    public Page<Review> getReviewsByReviewLike(Long movieId, Pageable pageable)
    {
        // 좋아요 순으로 정렬된 리뷰 정보들 반환
        return reviewRepository.findTopReviewsByMovieId(movieId, pageable);
    }

    public Page<Review> getReviewsByReviewRatingDesc(Long movieId, Pageable pageable)
    {
        // 별점 높은 순으로 정렬된 리뷰 정보들 반환
        return reviewRepository.findTopRatedReviewsByMovieId(movieId, pageable);
    }

    public Page<Review> getReviewsByReviewRatingAsc(Long movieId, Pageable pageable)
    {
        // 별점 낮은 순으로 정렬된 리뷰 정보들 반환
        return reviewRepository.findLowestRatedReviewsByMovieId(movieId, pageable);
    }

    public Page<Review> getReviewsByCreatedDateDesc(Long movieId, Pageable pageable)
    {
        // 작성 최신순으로 정렬된 리뷰 정보들 반환
        return reviewRepository.findRecentReviewsByMovieId(movieId, pageable);
    }

    public BigDecimal getReviewRatingById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        return review.getReviewRating(); // 리뷰의 평점 반환
    }

    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
    }

    public boolean hasReview(User user, Long movieId) {
        return reviewRepository.existsByUserAndMovie(user, movieId);
    }
}
