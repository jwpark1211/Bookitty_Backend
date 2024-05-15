package capstone.bookitty.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="star")
public class Star {

    @Id @Column(name = "star_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String isbn;
    private double score;

    @DateTimeFormat(pattern = "yyyy-mm-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @DateTimeFormat(pattern = "yyyy-mm-dd'T'HH:mm:ss")
    private LocalDateTime modifiedAt;

    @Builder
    public Star(Member member, String isbn, double score, LocalDateTime createdAt) {
        this.member = member;
        this.isbn = isbn;
        this.score = score;
        this.createdAt = createdAt;
    }

    public void updateStar(double score){
        this.modifiedAt = LocalDateTime.now();
        this.score = score;
    }
}
