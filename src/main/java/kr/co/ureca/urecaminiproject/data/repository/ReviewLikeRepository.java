package kr.co.ureca.urecaminiproject.data.repository;

import kr.co.ureca.urecaminiproject.data.entity.Review;
import kr.co.ureca.urecaminiproject.data.entity.ReviewLike;
import kr.co.ureca.urecaminiproject.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    ReviewLike findByReviewAndUser(Review review, User user);

    /**
     * 특정 리뷰와 사용자에 대한 좋아요가 존재하는지 확인합니다.
     * @param review 리뷰
     * @param user 사용자
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByReviewAndUser(Review review, User user);
}
