package capstone.bookitty.domain.bookSimilarity.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RatingEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private String isbn;
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public RatingEvent(Long memberId, String isbn){
        this.memberId = memberId;
        this.isbn = isbn;
    }

    public enum Status{
        PENDING,
        DONE
    }

    public void eventDone(){
        this.status = Status.DONE;
    }
}
