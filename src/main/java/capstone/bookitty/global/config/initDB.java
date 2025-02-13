package capstone.bookitty.global.config;

import capstone.bookitty.domain.bookState.entity.BookState;
import capstone.bookitty.domain.bookState.entity.State;
import capstone.bookitty.domain.comment.domain.Comment;
import capstone.bookitty.domain.comment.domain.Like;
import capstone.bookitty.domain.member.domain.Gender;
import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.star.domain.Star;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class initDB {
    private final InitService initService;

    @PostConstruct
    public void init(){
        initService.dbInit();
    }

    @Component
    @Transactional("dataTransactionManager")
    @RequiredArgsConstructor
    static class InitService{

        private final EntityManager em;
        private final PasswordEncoder pwEncoder;

        public void dbInit(){
            List<Member> members = Arrays.asList(
                    new Member("김민준", "alswns@gmail.com", pwEncoder.encode("Wo1902!si1"), null, Gender.MALE, LocalDate.of(1992, 7, 21)),
                    new Member("이서현", "dltjgus@gmail.com", pwEncoder.encode("Wo1902!si2"), null, Gender.FEMALE, LocalDate.of(2010, 12, 8)),
                    new Member("서진호", "tjwlsgn@gmail.com", pwEncoder.encode("Wo1902!si3"), null, Gender.MALE, LocalDate.of(1971, 8, 28)),
                    new Member("이선희", "dltjsgml@gmail.com", pwEncoder.encode("Wo1902!si4"), null, Gender.FEMALE, LocalDate.of(1969, 2, 5)),
                    new Member("신준서", "tlswnstj@gmail.com", pwEncoder.encode("Wo1902!si5"), null, Gender.MALE, LocalDate.of(2005, 8, 20)),
                    new Member("문다연", "ansekdusss@gmail.com", pwEncoder.encode("Wo1902!si6"), null, Gender.FEMALE, LocalDate.of(1999, 1, 14)),
                    new Member("윤동현", "dbsehdgus@gmail.com", pwEncoder.encode("Wo1902!si7"), null, Gender.MALE, LocalDate.of(1989, 7, 3)),
                    new Member("송지은", "thdwldms@gmail.com", pwEncoder.encode("Wo1902!si8"), null, Gender.FEMALE, LocalDate.of(1995, 3, 18)),
                    new Member("김준서", "rlawnstj@gmail.com", pwEncoder.encode("Wo1902!si9"), null, Gender.MALE, LocalDate.of(2001, 12, 11)),
                    new Member("임지민", "dlawlals@gmail.com", pwEncoder.encode("Wo1902!si10"), null, Gender.FEMALE, LocalDate.of(2009, 3, 14)),
                    new Member("안지성", "dkswltjd@gmail.com", pwEncoder.encode("Wo1902!si11"), null, Gender.MALE, LocalDate.of(2002, 8, 8)),
                    new Member("황예린", "ghkddPfls@gmail.com", pwEncoder.encode("Wo1902!si12"), null, Gender.FEMALE, LocalDate.of(1991, 11, 28)),
                    new Member("송현우", "thdgusdn@gmail.com", pwEncoder.encode("Wo1902!si13"), null, Gender.MALE, LocalDate.of(1999, 9, 8)),
                    new Member("정우진", "wjddnwls@gmail.com", pwEncoder.encode("Wo1902!si14"), null, Gender.MALE, LocalDate.of(1964, 1, 13)),
                    new Member("서은우", "tjdnsdmj@gmail.com", pwEncoder.encode("Wo1902!si15"), null, Gender.MALE, LocalDate.of(2008, 2, 20)),
                    new Member("이예진","dldPwls@gmail.com", pwEncoder.encode("Wo1902!si16"),null,Gender.FEMALE,LocalDate.of(2001,2,16))
            );

            for (Member member : members) {
                em.persist(member);
            }

            // Stars - 테스트 데이터 개선
            List<Star> stars = Arrays.asList(
                    // 책 A: 여러 명이 평가, 4~5점 중심
                    new Star(members.get(0), "9788952776372", 5.0),
                    new Star(members.get(1), "9788952776372", 4.5),
                    new Star(members.get(2), "9788952776372", 4.0),
                    new Star(members.get(3), "9788952776372", 4.0),
                    new Star(members.get(4), "9788952776372", 5.0),

                    // 책 B: 책 A와 유사한 점수 분포 (높은 유사도 기대)
                    new Star(members.get(0), "9791189327156", 5.0),
                    new Star(members.get(1), "9791189327156", 4.5),
                    new Star(members.get(2), "9791189327156", 4.0),
                    new Star(members.get(3), "9791189327156", 4.0),
                    new Star(members.get(4), "9791189327156", 4.5),

                    // 책 C: 책 A/B와 다른 패턴 (유사도 낮음 예상)
                    new Star(members.get(5), "9791198686114", 2.0),
                    new Star(members.get(6), "9791198686114", 3.0),
                    new Star(members.get(7), "9791198686114", 3.5),
                    new Star(members.get(8), "9791198686114", 2.5),
                    new Star(members.get(9), "9791198686114", 3.0),

                    // 책 D: 책 A/B와 일부 겹치지만 점수 차이가 큼 (중간 유사도 예상)
                    new Star(members.get(10), "9788954697941", 5.0),
                    new Star(members.get(11), "9788954697941", 5.0),
                    new Star(members.get(12), "9788954697941", 3.0),
                    new Star(members.get(13), "9788954697941", 3.5),
                    new Star(members.get(14), "9788954697941", 4.0),

                    // 책 E: 완전히 다른 점수 패턴 (매우 낮은 유사도 예상)
                    new Star(members.get(0), "9788936425012", 1.0),
                    new Star(members.get(1), "9788936425012", 1.5),
                    new Star(members.get(2), "9788936425012", 2.0),
                    new Star(members.get(3), "9788936425012", 2.5),
                    new Star(members.get(4), "9788936425012", 1.0),

                    // 책 F: 일부는 책 A와 비슷, 일부는 책 C와 비슷 (중간 유사도 예상)
                    new Star(members.get(5), "9788962622706", 5.0),
                    new Star(members.get(6), "9788962622706", 4.0),
                    new Star(members.get(7), "9788962622706", 3.0),
                    new Star(members.get(8), "9788962622706", 2.5),
                    new Star(members.get(9), "9788962622706", 3.5),

                    // 책 G: 책 A와 완전히 동일한 평가 (유사도 1.0 예상)
                    new Star(members.get(0), "9791141020866", 5.0),
                    new Star(members.get(1), "9791141020866", 4.5),
                    new Star(members.get(2), "9791141020866", 4.0),
                    new Star(members.get(3), "9791141020866", 4.0),
                    new Star(members.get(4), "9791141020866", 5.0),

                    // 책 H: 일부는 책 A/B와 비슷, 일부는 책 E와 비슷 (복합적인 유사도 예상)
                    new Star(members.get(10), "9791169092203", 5.0),
                    new Star(members.get(11), "9791169092203", 4.5),
                    new Star(members.get(12), "9791169092203", 2.0),
                    new Star(members.get(13), "9791169092203", 1.5),
                    new Star(members.get(14), "9791169092203", 3.0),

                    // 책 I: 책 A와 비슷한 패턴 (높은 유사도 예상, 공통 데이터 증가)
                    new Star(members.get(0), "9788998441012", 4.5),
                    new Star(members.get(1), "9788998441012", 4.0),
                    new Star(members.get(2), "9788998441012", 5.0),
                    new Star(members.get(3), "9788998441012", 4.5),
                    new Star(members.get(4), "9788998441012", 4.5),
                    new Star(members.get(5), "9788998441012", 4.0),
                    new Star(members.get(6), "9788998441012", 4.5),
                    new Star(members.get(7), "9788998441012", 5.0),

                    // 책 J: 책 C와 비슷한 낮은 점수 패턴 (공통 데이터 증가로 다양성 확보)
                    new Star(members.get(8), "9791138483049", 2.0),
                    new Star(members.get(9), "9791138483049", 2.5),
                    new Star(members.get(10), "9791138483049", 3.0),
                    new Star(members.get(11), "9791138483049", 2.5),
                    new Star(members.get(12), "9791138483049", 3.0),
                    new Star(members.get(13), "9791138483049", 2.0),
                    new Star(members.get(14), "9791138483049", 3.5),
                    new Star(members.get(0), "9791138483049", 2.5),

                    // 책 K: 중간 정도 유사도 예상, 공통 데이터 증가
                    new Star(members.get(1), "9791165653330", 3.5),
                    new Star(members.get(2), "9791165653330", 4.0),
                    new Star(members.get(3), "9791165653330", 4.5),
                    new Star(members.get(4), "9791165653330", 3.0),
                    new Star(members.get(5), "9791165653330", 3.0),
                    new Star(members.get(6), "9791165653330", 3.5),
                    new Star(members.get(7), "9791165653330", 4.0),
                    new Star(members.get(8), "9791165653330", 3.5),

                    // 책 L: 랜덤 점수 분포, 공통 데이터 추가 (낮은 유사도 예상)
                    new Star(members.get(9), "9791130646381", 3.0),
                    new Star(members.get(10), "9791130646381", 4.5),
                    new Star(members.get(11), "9791130646381", 1.5),
                    new Star(members.get(12), "9791130646381", 5.0),
                    new Star(members.get(13), "9791130646381", 2.0),
                    new Star(members.get(14), "9791130646381", 3.5),
                    new Star(members.get(0), "9791130646381", 2.5),
                    new Star(members.get(1), "9791130646381", 4.0),

                    // 책 M: 혼합된 점수 분포, 공통 데이터 증가 (유사도 0.5~0.7 예상)
                    new Star(members.get(2), "9791193506530", 4.5),
                    new Star(members.get(3), "9791193506530", 4.0),
                    new Star(members.get(4), "9791193506530", 3.5),
                    new Star(members.get(5), "9791193506530", 2.0),
                    new Star(members.get(6), "9791193506530", 3.5),
                    new Star(members.get(7), "9791193506530", 3.0),
                    new Star(members.get(8), "9791193506530", 4.0),
                    new Star(members.get(9), "9791193506530", 3.0),

                    // 책 N: 책 G와 거의 동일한 점수 패턴 (유사도 1.0 예상)
                    new Star(members.get(10), "9791130698199", 5.0),
                    new Star(members.get(11), "9791130698199", 4.5),
                    new Star(members.get(12), "9791130698199", 4.0),
                    new Star(members.get(13), "9791130698199", 4.0),
                    new Star(members.get(14), "9791130698199", 5.0),
                    new Star(members.get(0), "9791130698199", 4.5),
                    new Star(members.get(1), "9791130698199", 4.0),
                    new Star(members.get(2), "9791130698199", 4.5),

                    // 책 O: 매우 낮은 점수 패턴, 공통 데이터 증가 (유사도 낮음 예상)
                    new Star(members.get(3), "9788937462788", 1.0),
                    new Star(members.get(4), "9788937462788", 1.5),
                    new Star(members.get(5), "9788937462788", 2.0),
                    new Star(members.get(6), "9788937462788", 2.0),
                    new Star(members.get(7), "9788937462788", 1.0),
                    new Star(members.get(8), "9788937462788", 1.5),
                    new Star(members.get(9), "9788937462788", 2.0),
                    new Star(members.get(10), "9788937462788", 1.5)
            );


            for (Star star : stars) {
                em.persist(star);
            }

            // Book States
            List<BookState> bookStates = Arrays.asList(
                    new BookState(members.get(0), "9791189327156", "국내도서>과학>기초과학/교양과학", "물고기는 존재하지 않는다 - 상실, 사랑 그리고 숨어 있는 삶의 질서에 관한 이야기",
                            "룰루 밀러 (지은이), 정지인 (옮긴이)", "https://image.aladin.co.kr/product/28465/73/cover200/k092835920_2.jpg", State.READ_ALREADY, LocalDateTime.now().minusDays(30)),
                    new BookState(members.get(0), "9791168127012", "국내도서>소설/시/희곡>한국소설>2000년대 이후 한국소설", "파쇄",
                            "구병모 (지은이)", "https://image.aladin.co.kr/product/31273/29/cover200/k592832565_1.jpg", State.READ_ALREADY, LocalDateTime.now().minusDays(35)),
                    new BookState(members.get(0), "9791191056372", "국내도서>경제경영>재테크/투자>재테크/투자 일반", "돈의 심리학 (30만 부 기념 스페셜 에디션) - 당신은 왜 부자가 되지 못했는가",
                            "모건 하우절 (지은이), 이지연 (옮긴이)", "https://image.aladin.co.kr/product/32852/7/cover200/k132936910_2.jpg", State.READING,null),
                    new BookState(members.get(0), "9788925588735", "국내도서>소설/시/희곡>과학소설(SF)>외국 과학소설", "프로젝트 헤일메리",
                            "앤디 위어 (지은이), 강동혁 (옮긴이)", "https://image.aladin.co.kr/product/27045/43/cover200/8925588730_1.jpg", State.READ_ALREADY, LocalDateTime.now().minusDays(1)),
                    new BookState(members.get(0), "9791172450519", "국내도서>인문학>철학 일반>교양 철학", "니체가 일각돌고래라면 - 인간이 만물의 영장이라는 편견에 대하여",
                            "저스틴 그레그 (지은이), 김아림 (옮긴이)", "https://image.aladin.co.kr/product/33855/68/cover200/k302930207_1.jpg", State.WANT_TO_READ, null),
                    new BookState(members.get(0), "9791168473690", "국내도서>자기계발>성공>성공학", "세이노의 가르침 (화이트 에디션) - 피보다 진하게 살아라",
                            "세이노(SayNo) (지은이)", "https://image.aladin.co.kr/product/30929/51/cover200/s662930344_1.jpg", State.WANT_TO_READ, null),
                    new BookState(members.get(0), "9791172130374", "국내도서>소설/시/희곡>한국소설>2000년대 이후 한국소설", "원도",
                            "최진영 (지은이)", "https://image.aladin.co.kr/product/33657/84/cover200/k232939529_1.jpg", State.READ_ALREADY, LocalDateTime.now().minusDays(60)),
                    new BookState(members.get(1), "9791189327156", "국내도서>과학>기초과학/교양과학", "물고기는 존재하지 않는다 - 상실, 사랑 그리고 숨어 있는 삶의 질서에 관한 이야기",
                            "룰루 밀러 (지은이), 정지인 (옮긴이)", "https://image.aladin.co.kr/product/28465/73/cover200/k092835920_2.jpg", State.READING, null),
                    new BookState(members.get(2), "9791189327156", "국내도서>과학>기초과학/교양과학", "물고기는 존재하지 않는다 - 상실, 사랑 그리고 숨어 있는 삶의 질서에 관한 이야기",
                            "룰루 밀러 (지은이), 정지인 (옮긴이)", "https://image.aladin.co.kr/product/28465/73/cover200/k092835920_2.jpg", State.WANT_TO_READ, null),
                    new BookState(members.get(3), "9791189327156", "국내도서>과학>기초과학/교양과학", "물고기는 존재하지 않는다 - 상실, 사랑 그리고 숨어 있는 삶의 질서에 관한 이야기",
                            "룰루 밀러 (지은이), 정지인 (옮긴이)", "https://image.aladin.co.kr/product/28465/73/cover200/k092835920_2.jpg", State.READ_ALREADY, LocalDateTime.now().minusDays(10)),
                    new BookState(members.get(4), "9788901276533", "국내도서>에세이>외국에세이", "나는 메트로폴리탄 미술관의 경비원입니다 - 경이로운 세계 속으로 숨어버린 한 남자의 이야기",
                            "패트릭 브링리 (지은이), 김희정, 조현주 (옮긴이)", "https://image.aladin.co.kr/product/32892/38/cover200/s402930162_1.jpg", State.READING, null),
                    new BookState(members.get(5), "9788901276533", "국내도서>에세이>외국에세이", "나는 메트로폴리탄 미술관의 경비원입니다 - 경이로운 세계 속으로 숨어버린 한 남자의 이야기",
                            "패트릭 브링리 (지은이), 김희정, 조현주 (옮긴이)", "https://image.aladin.co.kr/product/32892/38/cover200/s402930162_1.jpg", State.WANT_TO_READ, null),
                    new BookState(members.get(6), "9788901276533", "국내도서>에세이>외국에세이", "나는 메트로폴리탄 미술관의 경비원입니다 - 경이로운 세계 속으로 숨어버린 한 남자의 이야기",
                            "패트릭 브링리 (지은이), 김희정, 조현주 (옮긴이)", "https://image.aladin.co.kr/product/32892/38/cover200/s402930162_1.jpg", State.READ_ALREADY, LocalDateTime.now().minusDays(3)),
                    new BookState(members.get(7), "9788957365793", "국내도서>경제경영>경제학/경제일반>경제이론/경제사상", "EBS 다큐프라임 자본주의",
                            "EBS 자본주의 제작팀 (지은이)", "https://image.aladin.co.kr/product/3164/26/cover200/8957365796_1.jpg", State.READING, null),
                    new BookState(members.get(8), "9788957365793", "국내도서>경제경영>경제학/경제일반>경제이론/경제사상", "EBS 다큐프라임 자본주의",
                            "EBS 자본주의 제작팀 (지은이)", "https://image.aladin.co.kr/product/3164/26/cover200/8957365796_1.jpg", State.WANT_TO_READ, null),
                    new BookState(members.get(9), "9788957365793", "국내도서>경제경영>경제학/경제일반>경제이론/경제사상", "EBS 다큐프라임 자본주의",
                            "EBS 자본주의 제작팀 (지은이)", "https://image.aladin.co.kr/product/3164/26/cover200/8957365796_1.jpg", State.READ_ALREADY, LocalDateTime.now().minusDays(5))
            );

            for (BookState bookState : bookStates) {
                em.persist(bookState);
            }

            // Comments
            List<Comment> comments = Arrays.asList(
                    new Comment(members.get(0), "9791189327156", "자연엔 순위가 없다. 다윈이 말한 것처럼 문명화된 인간들은 약자들을 제거하는 과정을 최대한 자제하려 한다고 말한다. 어울려 살아가며 서로의 존재가치를 인정하는 것, 무엇인가에 우위를 나누지 않는 것에 대해 생각하게 된다.\n" +
                            "이 사다리, 그것은 아직도 살아 있다.\n" +
                            "이 사다리, 그것은 위험한 허구다.\n" +
                            "물고기는 존재하지 않는다.\n" +
                            "이 말은 그 허구를 쪼개버릴 물고기모양의 대형망치다.(268쪽)!"),
                    new Comment(members.get(1), "9791189327156", "잘 읽히긴 하는데...이렇게 극찬 받을 책인가 하는 생각이 들었다. 한 번 읽어봤으면 됐다 싶은 정도"),
                    new Comment(members.get(2), "9791189327156", "2022 올해의 책으로 유명했던데 정말 구성도 재밌고 이 책의 장르도 이것 저것 믹스되면서 독특한 재미가 있습니다. 친구들에게도 많이 추천했어요.\n" +
                            "대신 스포없이 처음부터 끝까지 다 읽는게 좋기 때문에 미리 정보를 찾아보지 않는게 중요한거 같습니다."),
                    new Comment(members.get(3), "9791189327156", "중요한 개개인이라는 존재의 만남, 선물같은 생명들"),
                    new Comment(members.get(4), "9788901276533", "생각보다 지루한 부분이 많았지만 미술관의 규모와 다양하고 방대한 소장품에 대해 관심을 갖게 하고 복제품이 아닌 모두 진품이라는 사실에 메트에 가보고 싶게 만드는 책."),
                    new Comment(members.get(5), "9788901276533", "마음이 힘들고 울적하고 우울할 때 이 책을 읽으면 치유돼요." +
                            "세계 3대 미술관’이라 불리는 메트로폴리탄 미술관을 배경으로 한 도서예요"),
                    new Comment(members.get(6), "9788901276533", "비틀기와 뒤집기가 적절히 배치된 선동형 책.\n" +
                            "한 인물을 집요하게 탐구했던 저자의 의도가 정말 무엇이었을까?"),
                    new Comment(members.get(7), "9788957365793", "일부는 밑줄도 긋고, 공부하듯 잘 보았습니다."),
                    new Comment(members.get(8), "9788957365793", "책 진짜 좋아요 굿굿"),
                    new Comment(members.get(9), "9788957365793", "사회의 시스템을 이해하기에 적합한 책,\n" +
                            "툴러플래그로 밑줄치면서 학창시절 공부하던 것처럼 읽었다.")
            );

            for (Comment comment : comments) {
                em.persist(comment);
            }

            // Likes
            List<Like> likes = Arrays.asList(
                    new Like(members.get(0), comments.get(0)),
                    new Like(members.get(1), comments.get(0)),
                    new Like(members.get(2), comments.get(0)),
                    new Like(members.get(1), comments.get(1)),
                    new Like(members.get(2), comments.get(2)),
                    new Like(members.get(3), comments.get(3)),
                    new Like(members.get(4), comments.get(4)),
                    new Like(members.get(5), comments.get(5)),
                    new Like(members.get(6), comments.get(6)),
                    new Like(members.get(7), comments.get(7)),
                    new Like(members.get(8), comments.get(8)),
                    new Like(members.get(9), comments.get(9))
            );

            for (Like like : likes) {
                em.persist(like);
            }

        }


    }
}
