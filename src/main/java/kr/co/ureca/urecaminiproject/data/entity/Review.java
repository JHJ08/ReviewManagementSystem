package kr.co.ureca.urecaminiproject.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(precision = 2, scale = 1)
    @ColumnDefault("0.0")
    private BigDecimal reviewRating;

    @Column(length = 10000)
    private String reviewContent;

    @Column(nullable = false)
    private LocalDateTime reviewCreatedAt;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @ToString.Exclude
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ReviewLike> reviewLikes = new ArrayList<>();

    public void addLike(User user) {
        // 사용자가 이미 좋아요를 눌렀는지 확인
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setReview(this); // 현재 리뷰를 설정
        reviewLike.setUser(user);   // 좋아요를 누른 사용자 설정

        // reviewLikes Set에 추가
        this.reviewLikes.add(reviewLike);
        this.likeCount = (long) reviewLikes.size();
    }

    public void removeLike(User user) {
        // 사용자가 누른 좋아요를 찾기
        ReviewLike likeToRemove = reviewLikes.stream()
                .filter(like -> like.getUser().equals(user))
                .findFirst()
                .orElse(null);

        if (likeToRemove != null) {
            // reviewLikes Set에서 제거
            reviewLikes.remove(likeToRemove);
        }
        this.likeCount = (long) reviewLikes.size();
    }

    @Transient
    private Long likeCount; // 이 필드는 JPA가 아닌 수동으로 관리하는 필드


}
