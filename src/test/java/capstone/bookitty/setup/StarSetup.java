package capstone.bookitty.setup;

import capstone.bookitty.domain.entity.Member;
import capstone.bookitty.domain.entity.Star;
import capstone.bookitty.domain.repository.StarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;


@RequiredArgsConstructor
@Component
public class StarSetup {
    private final MemberSetup memberSetup;
    private final StarRepository starRepository;

    public Star save(){
        final Member member = memberSetup.save();
        final Star star = buildStar(member, "isbn",5);
        return starRepository.save(star);
    }

    public List<Star> save(int count){
        List<Star> stars = new ArrayList<>();
        List<Member> members = memberSetup.save(count);
        double[] scores = {0.5,1,1.5,2,2.5,3,3.5,4,4.5,5};
        IntStream.range(0,count).forEach(i-> stars.add(
                starRepository.save(buildStar(
                        members.get(i), String.format("isbn%d",i),scores[i%9]))));
        return stars;
    }

    public List<Star> save_oneMemberManyStars(int count){
        List<Star> stars = new ArrayList<>();
        final Member member  = memberSetup.save();
        double[] scores = {0.5,1,1.5,2,2.5,3,3.5,4,4.5,5};
        IntStream.range(0,count).forEach(i-> stars.add(
                starRepository.save(buildStar(
                        member, String.format("isbn%d",i),scores[i%9]))));
        return stars;
    }

    public List<Star> save_oneIsbnManyStars(int count){
        List<Star> stars = new ArrayList<>();
        List<Member> members = memberSetup.save(count);
        double[] scores = {0.5,1,1.5,2,2.5,3,3.5,4,4.5,5};
        IntStream.range(0,count).forEach(i-> stars.add(
                starRepository.save(buildStar(
                        members.get(i), "isbn",scores[i%9]))));
        return stars;
    }

    private Star buildStar(Member member, String isbn, double score){
        return Star.builder()
                .member(member)
                .isbn(isbn)
                .score(score)
                .build();
    }
}
