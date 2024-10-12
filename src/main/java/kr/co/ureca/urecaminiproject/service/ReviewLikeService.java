package kr.co.ureca.urecaminiproject.service;

import kr.co.ureca.urecaminiproject.data.entity.Review;
import kr.co.ureca.urecaminiproject.data.entity.ReviewLike;
import kr.co.ureca.urecaminiproject.data.entity.User;
import kr.co.ureca.urecaminiproject.data.repository.ReviewLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {
    private final ReviewLikeRepository reviewLikeRepository;

    public boolean existsLikeByReviewAndUser(Review review, User user) {
        return reviewLikeRepository.existsByReviewAndUser(review, user);
    }

    public void addReviewLike(Review review, User user) {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setReview(review);
        reviewLike.setUser(user);
        reviewLikeRepository.save(reviewLike);
    }

    public void cancelReviewLike(Review review, User user) {

        ReviewLike reviewLike = reviewLikeRepository.findByReviewAndUser(review, user);
        if (reviewLike != null) {
            reviewLikeRepository.delete(reviewLike);
        }
    }
}
