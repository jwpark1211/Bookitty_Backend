package capstone.bookitty.domain.bookState.entity;

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
@Table(name = "book_state")
public class BookState {

    @Id @Column(name = "state_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    private String isbn;
    private String categoryName;
    private String bookTitle;
    private String bookAuthor;
    private String bookImgUrl;

    @Enumerated(EnumType.STRING)
    private State state;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readAt;

    @Builder
    public BookState(Member member, String isbn, String categoryName, String bookTitle,
                     String bookAuthor, String bookImgUrl, State state, LocalDateTime readAt) {
        this.member = member;
        this.isbn = isbn;
        this.categoryName = categoryName;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookImgUrl = bookImgUrl;
        this.state = state;
        this.readAt = readAt;
    }

    public void updateState(State state){
        this.state = state;
        if(state == State.READ_ALREADY) readAtNow();
    }
    public void readAtNow(){readAt = LocalDateTime.now();}

}
