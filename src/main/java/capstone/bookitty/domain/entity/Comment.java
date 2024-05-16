package capstone.bookitty.domain.entity;

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
@Table(name="comment")
public class Comment {

    @Id @Column(name = "comment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    private String isbn;
    private String content;

    @DateTimeFormat(pattern = "yyyy-mm-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @DateTimeFormat(pattern = "yyyy-mm-dd'T'HH:mm:ss")
    private LocalDateTime modifiedAt;

    @Builder
    public Comment(Member member, String isbn, String content) {
        this.member = member;
        this.isbn = isbn;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public void updateContent(String content){
        this.content = content;
        this.modifiedAt = LocalDateTime.now();
    }

}
