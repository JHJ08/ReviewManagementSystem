package kr.co.ureca.urecaminiproject.data.dto;


import kr.co.ureca.urecaminiproject.data.entity.Review;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString
public class ReviewDTO {

    // 폼 입력 및 정보 전시에 필요한 정보 모음
    private Long reviewId; // 리뷰 아이디
    private Long movieId; // 영화 아이디

    private String movieTitle; // 영화 제목
    private String userName; // 사용자 이름
    private String userNickName; // 사용자 닉네임
    private float reviewRating; // 리뷰 별점
    private String reviewContent; // 리뷰 내용
    private LocalDateTime reviewCreatedAt; // 리뷰 작성 날짜
    private String formattedDate; // 리뷰 작성 날짜 전시 형식으로 변경한 문자열 - 'YYYY-mm-dd'

    private Long likeCount; // 좋아요 개수


    public static ReviewDTO toDTO(Review review) {
        // Review 엔티티를 ReviewDTO로 변환하는 함수

        ReviewDTO dto = new ReviewDTO();

        dto.setReviewId(review.getReviewId());

        dto.setMovieId(review.getMovie().getMovieId());

        dto.setMovieTitle(review.getMovie().getMovieTitle());

        dto.setUserName(review.getUser().getUserName());
        dto.setUserNickName(review.getUser().getUserRealName());

        dto.setReviewRating(review.getReviewRating().floatValue());

        dto.setReviewContent(review.getReviewContent());

        dto.setReviewCreatedAt(review.getReviewCreatedAt());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dto.setFormattedDate(review.getReviewCreatedAt().format(formatter));


        dto.setLikeCount((long) review.getReviewLikes().size());

        return dto;
    }


}
