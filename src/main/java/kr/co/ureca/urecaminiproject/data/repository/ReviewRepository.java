package kr.co.ureca.urecaminiproject.data.repository;

import kr.co.ureca.urecaminiproject.data.entity.Review;
import kr.co.ureca.urecaminiproject.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>{
    List<Review> findByMovieMovieId(Long movieId);

    // 좋아요 순 정렬 (기본) - 페이징 처리
    @Query("SELECT r FROM Review r WHERE r.movie.movieId = :movieId " +
            "ORDER BY " +
            "SIZE(r.reviewLikes) DESC, " + // 좋아요 순 정렬
            "r.reviewCreatedAt DESC") // 최신순 정렬
    Page<Review> findTopReviewsByMovieId(@Param("movieId") Long movieId, Pageable pageable);


    // 높은 평가 순 정렬
    @Query("SELECT r FROM Review r WHERE r.movie.movieId = :movieId " +
            "ORDER BY " +
            "r.reviewRating DESC, " +        // 별점 높은 순으로 정렬
            "SIZE(r.reviewLikes) DESC, " +   // 좋아요 순 정렬
            "r.reviewCreatedAt DESC")        // 최신순 정렬
    Page<Review> findTopRatedReviewsByMovieId(@Param("movieId") Long movieId, Pageable pageable);

    // 낮은 평가 순 정렬
    @Query("SELECT r FROM Review r WHERE r.movie.movieId = :movieId " +
            "ORDER BY " +
            "r.reviewRating ASC, " +        // 별점 낮은 순으로 정렬
            "SIZE(r.reviewLikes) DESC, " +   // 좋아요 순 정렬
            "r.reviewCreatedAt DESC")        // 최신순 정렬
    Page<Review> findLowestRatedReviewsByMovieId(@Param("movieId") Long movieId, Pageable pageable);

    // 최신 순 정렬
    @Query("SELECT r FROM Review r WHERE r.movie.movieId = :movieId " +
            "ORDER BY " +
            "r.reviewCreatedAt DESC, " + // 최신순 정렬
            "SIZE(r.reviewLikes) DESC") // 좋아요 순 정렬
    Page<Review> findRecentReviewsByMovieId(@Param("movieId") Long movieId, Pageable pageable);


    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie.movieId = :movieId")
    long countReviewsByMovieId(@Param("movieId") Long movieId);

    //boolean existsByUserAndMovie(kr.co.ureca.urecaminiproject.data.entity.User user, Long movieId);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.user = ?1 AND r.movie.movieId = ?2")
    boolean existsByUserAndMovie(User user, Long movieId);
}
