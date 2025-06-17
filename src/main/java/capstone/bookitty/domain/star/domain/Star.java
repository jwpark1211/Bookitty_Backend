package capstone.bookitty.domain.star.domain;

import capstone.bookitty.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "star",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_member_isbn", columnNames = {"member_id", "isbn"})
        }
)
public class Star {

    @Id @Column(name = "star_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    private String isbn;

    @Getter(AccessLevel.NONE)
    private int score;

    @Version
    private int version;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modifiedAt;

    @Builder
    public Star(Member member, String isbn, double score) {
        this.member = member;
        this.isbn = isbn;
        this.score = toInternalScore(score);
        this.createdAt = LocalDateTime.now();
    }

    public void updateStar(double score){
        this.modifiedAt = LocalDateTime.now();
        this.score = toInternalScore(score);
    }

    /** 사용자에게 보여줄 점수 (예: 7 → 3.5) */
    public double getScore() {
        return score / 2.0;
    }

    /** 내부적으로 저장할 점수로 변환 (예: 3.5 → 7) */
    private int toInternalScore(double inputScore) {
        return (int) (inputScore * 2);
    }
}
