package capstone.bookitty.setup;

import capstone.bookitty.domain.entity.BookState;
import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.State;
import capstone.bookitty.domain.repository.BookStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Component
public class BookStateSetup {
    private final MemberSetup memberSetup;
    private final BookStateRepository stateRepository;

    public BookState save(){
        final Member member = memberSetup.save();
        final BookState state = buildState(member,"isbn1");
        return stateRepository.save(state);
    }

    public List<BookState> save(int count){
        List<BookState> states = new ArrayList<>();
        List<Member> members = memberSetup.save(count);
        IntStream.range(0,count).forEach(i->states.add(
                stateRepository.save(buildState(
                        members.get(i),
                        String.format("isbn%d",i)))));
        return states;
    }

    private BookState buildState(Member member,String isbn){
        return BookState.builder()
                .state(State.READ_ALREADY)
                .readAt(LocalDateTime.now())
                .bookAuthor("author")
                .bookImgUrl("imageUrl")
                .bookTitle("title")
                .categoryName("category")
                .isbn(isbn)
                .member(member)
                .build();
    }
}
