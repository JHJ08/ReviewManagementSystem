package kr.co.ureca.urecaminiproject.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.ureca.urecaminiproject.data.dto.ReviewDTO;
import kr.co.ureca.urecaminiproject.data.entity.Movie;
import kr.co.ureca.urecaminiproject.data.entity.Review;
import kr.co.ureca.urecaminiproject.data.entity.User;
import kr.co.ureca.urecaminiproject.data.repository.DirectorRepository;
import kr.co.ureca.urecaminiproject.data.repository.UserRepository;
import kr.co.ureca.urecaminiproject.service.MovieService;
import kr.co.ureca.urecaminiproject.service.ReviewLikeService;
import kr.co.ureca.urecaminiproject.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private UserRepository userRepository; // 추후 수정 필요

    @Autowired
    private ReviewLikeService reviewLikeService;
    @Autowired
    private DirectorRepository directorRepository;

    @GetMapping("/movies/{movieId}")
    public String showMovieDetail(@PathVariable("movieId") Long movieId, Model model) {

        // 영화 아이디에 해당하는 영화 상세 정보 전시
        Movie movie = movieService.findMoviebyId(movieId);
        List<String> directorNames = movieService.findDirectorNamesByMovieId(movieId);
        List<String> actorsNames = movieService.findActorNamesByMovieId(movieId);
        List<String> genreName = movieService.findGenreNamesByMovieId(movieId);

        if(movie != null)
        {
            // 영화 아이디 및 영화 제목 추가
            model.addAttribute("movieId", movieId);
            model.addAttribute("movieTitle", movie.getMovieTitle());
            model.addAttribute("movie", movie);

            model.addAttribute("directorNames", directorNames);
            model.addAttribute("actorsNames", actorsNames);
            model.addAttribute("genreName", genreName);
            // 현재 등록된 해당 영화의 일부 리뷰 정보 가져오기 (0개 ~ 8개)
            Pageable pageable = PageRequest.of(0, 8); // 페이지 번호 0, 페이지 크기 8
            Page<Review> partialReviews = reviewService.getReviewsByReviewLike(movieId, pageable); // 일부만 가져오게 처리하는 로직은 서비스에 들어가도록 처리 필요
            model.addAttribute("partialReviews", partialReviews.map(ReviewDTO::toDTO));
            model.addAttribute("reviewsCount",reviewService.getReviewCountByMovieId(movieId));

            // 영화 평균 총점 계산
            BigDecimal totalRating = movieService.findMoviebyId(movieId).getMovieTotalRating();
            long reviewCount = reviewService.getReviewCountByMovieId(movieId);

            BigDecimal averageRating = reviewCount > 0
                    ? totalRating.divide(BigDecimal.valueOf(reviewCount), RoundingMode.HALF_UP)
                    : BigDecimal.ZERO; // 리뷰가 없을 경우 0으로 설정

            String averageRatingDisplay = averageRating.setScale(1, RoundingMode.HALF_UP).toString();
            model.addAttribute("averageRating", averageRatingDisplay);
        }

        return "movie_detail";
    }

    @PostMapping("/movies/{movieId}/reviews")
    public String addReview(@PathVariable("movieId") Long movieId, ReviewDTO reviewDTO, Principal principal)
    {
        // 리뷰 등록 처리

        //System.out.println(principal.getName());

        reviewService.createReview(userRepository.findByUserName(principal.getName()), movieService.findMoviebyId(movieId), reviewDTO.getReviewRating(), reviewDTO.getReviewContent());

        movieService.addTotalReviewRating(movieId, reviewDTO.getReviewRating());

        return "redirect:/movies/"+reviewDTO.getMovieId();
    }

    @GetMapping("/reviews/{reviewId}")
    public String showReviewDetail(@PathVariable Long reviewId,
                                   @RequestParam(required = false) String from,
                                   Model model) {

        ReviewDTO reviewDTO = ReviewDTO.toDTO(reviewService.findReviewById(reviewId));
        model.addAttribute("reviewDTO", reviewDTO);
        model.addAttribute("prevPage", from);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthor = false;
        // 로그인된 경우
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            // 인증된 사용자
            String loginUserName = authentication.getName();

            isAuthor = loginUserName.equals(reviewDTO.getUserName());

            /*if(loginUserName.equals(reviewDTO.getUserName()))
            {
                System.out.println("현재 로그인한 사용자와 작성자가 동일한 경우");
            }
            else
            {
                System.out.println("현재 로그인한 사용자와 작성자가 동일하지 않은 경우");
            }*/

            //System.out.println("[작성한 회원 정보]"+reviewDTO.getUserName());
            //System.out.println("[로그인한 회원 정보]"+loginUserName);
        }
        // 로그인 안한 경우
        //else
        //{
        //    System.out.println("로그인 안된 경우");
        //}

        model.addAttribute("isAuthor", isAuthor); // 작성자 여부 추가

        return "review_detail";  // 반환할 뷰 이름
    }

    @GetMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ReviewDTO fetchReviewById(@PathVariable Long reviewId) {

        // 리뷰 아이디에 해당하는 리뷰 정보 json 형태로 반환
        ReviewDTO reviewDTO = ReviewDTO.toDTO(reviewService.findReviewById(reviewId));
        return reviewDTO;
    }

    // 좋아요 처리
    @PostMapping("/api/reviews/{reviewId}/like")
    public ResponseEntity<Map<String, Object>> toggleReviewLike(@PathVariable Long reviewId, Principal principal) {

        if (principal == null) {
            //System.out.println("principal is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "로그인이 필요합니다."));
        }
        /*else {
            System.out.println("principal is " + principal.getName());
        }*/

        User currentUser = userRepository.findByUserName(principal.getName());
        boolean existingLike = reviewLikeService.existsLikeByReviewAndUser(reviewService.findReviewById(reviewId), currentUser);

        if (existingLike) {
            // 좋아요가 이미 존재하면 취소
            reviewService.cancelReviewLike(reviewId, currentUser);
            reviewLikeService.cancelReviewLike(reviewService.findReviewById(reviewId), currentUser);
        }
        else {
            // 좋아요가 존재하지 않으면 추가
            reviewService.addReviewLike(reviewId, currentUser);
            reviewLikeService.addReviewLike(reviewService.findReviewById(reviewId), currentUser);
        }

        long likeCount = reviewService.findReviewById(reviewId).getLikeCount();

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewDTO reviewDTO) {

        // 이전 평점 값 가져오기
        BigDecimal oldReviewRating = reviewService.getReviewRatingById(reviewId);

        reviewService.updateReview(reviewId, reviewDTO.getReviewRating(), reviewDTO.getReviewContent());

        movieService.updateTotalReviewRating(reviewDTO.getMovieId(),oldReviewRating, reviewDTO.getReviewRating());

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("reviewRating", reviewDTO.getReviewRating());
        response.put("reviewContent", reviewDTO.getReviewContent());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {

        // 삭제 전 삭제된 리뷰의 평점 값 가져오기
        // 삭제 전 삭제된 리뷰의 평점 값 가져오기
        Review review = reviewService.getReviewById(reviewId); // 리뷰 객체를 가져옴
        BigDecimal oldReviewRating = review.getReviewRating(); // 기존 평점 가져오기
        Long movieId = review.getMovie().getMovieId();

        // 매개변수로 입력받은 리뷰 아이디에 해당하는 리뷰 정보 삭제 후, 삭제 성공여부 반환
        boolean isDeleted = reviewService.deleteReview(reviewId);

        if (isDeleted) {

            // 평점 삭제 처리
            movieService.deleteTotalReviewRating(movieId,oldReviewRating);


            System.out.println("ok");
            return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
        } else {
            // 삭제 실패 시 상태 코드 404 반환
            System.out.println("fail");
            return ResponseEntity.status(404).body("리뷰를 찾을 수 없습니다.");
        }
    }

    @GetMapping("/movies/{movieId}/reviews")
    //@ResponseBody
    public String getReviewsByMovieId(Model model,
            @PathVariable("movieId") Long movieId,
            @RequestParam(value = "sort", defaultValue = "likes") String sortValue,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewsPage;
        switch (sortValue) {
            case "ratingLow":
                reviewsPage = reviewService.getReviewsByReviewRatingAsc(movieId, pageable);
                break;
            case "ratingHigh":
                reviewsPage = reviewService.getReviewsByReviewRatingDesc(movieId, pageable);
                break;
            case "date":
                reviewsPage = reviewService.getReviewsByCreatedDateDesc(movieId, pageable);
                break;
            default:
                reviewsPage = reviewService.getReviewsByReviewLike(movieId, pageable);
                break;
        }

        model.addAttribute("reviews", reviewsPage.map(ReviewDTO::toDTO));
        model.addAttribute("sort", sortValue);  // 정렬 값 추가

        return "review_list";
    }

    @GetMapping("/api/movies/{movieId}/reviews")
    @ResponseBody
    public Page<ReviewDTO> getSortedReviews(
            @PathVariable("movieId") Long movieId,
            @RequestParam(value = "sort", required = false) String sortValue,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        System.out.println("[확인]"+sortValue);

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewsPage;

        switch (sortValue) {
            case "ratingLow":
                reviewsPage = reviewService.getReviewsByReviewRatingAsc(movieId, pageable);
                break;
            case "ratingHigh":
                reviewsPage = reviewService.getReviewsByReviewRatingDesc(movieId, pageable);
                break;
            case "date":
                reviewsPage = reviewService.getReviewsByCreatedDateDesc(movieId, pageable);
                break;
            default:
                reviewsPage = reviewService.getReviewsByReviewLike(movieId, pageable);
                break;
        }

        return reviewsPage.map(ReviewDTO::toDTO);
    }

    @GetMapping("/api/isLoggedIn")
    public ResponseEntity<Map<String, Boolean>> checkLoginStatus(HttpSession session) {

        Map<String, Boolean> response = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isLoggedIn = false;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            // 인증된 사용자
            isLoggedIn = true;
        }

        response.put("isLoggedIn", isLoggedIn);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/checkReview/{movieId}")
    public ResponseEntity<Map<String, Boolean>> checkReview(
            @PathVariable Long movieId,
            Principal principal) {

        // 사용자 정보 가져오기
        User user = userRepository.findByUserName(principal.getName());

        // 리뷰 존재 여부 확인
        boolean hasReview = reviewService.hasReview(user, movieId);

        // Map 형태로 응답 생성
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasReview", hasReview);

        return ResponseEntity.ok(response);
    }




}
