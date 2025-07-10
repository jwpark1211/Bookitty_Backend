package capstone.bookitty.global;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.type.Authority;
import capstone.bookitty.domain.member.domain.type.Gender;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.global.authentication.PasswordEncoder;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitDB {
    private final InitService initService;

    //@PostConstruct
    public void init() {
        initService.dbInit();
    }

    @Component
    @Transactional("dataTransactionManager")
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;
        private final PasswordEncoder passwordEncoder;

        public void dbInit() {
            List<Member> members = Arrays.asList(
                    createUser("김민준", "alswns@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1992, 7, 21)),
                    createUser("이서현", "dltjgus@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2010, 12, 8)),
                    createUser("서진호", "tjwlsgn@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1971, 8, 28)),
                    createUser("이선희", "dltjsgml@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(1969, 2, 5)),
                    createUser("신준서", "tlswnstj@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(2005, 8, 20)),
                    createUser("문다연", "ansekdusss@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(1999, 1, 14)),
                    createUser("윤동현", "dbsehdgus@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1989, 7, 3)),
                    createUser("송지은", "thdwldms@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(1995, 3, 18)),
                    createUser("김준서", "rlawnstj@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(2001, 12, 11)),
                    createUser("임지민", "dlawlals@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2009, 3, 14)),
                    createUser("안지성", "dkswltjd@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(2002, 8, 8)),
                    createUser("황예린", "ghkddPfls@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(1991, 11, 28)),
                    createUser("송현우", "thdgusdn@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1999, 9, 8)),
                    createUser("정우진", "wjddnwls@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1964, 1, 13)),
                    createUser("서은우", "tjdnsdmj@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(2008, 2, 20)),
                    createUser("이예진", "dldPwls@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2001, 2, 16))
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

        }

        public Member createUser(String name, String email, String password,
                                 Gender gender, LocalDate birthDate) {
            return Member.builder()
                    .name(name)
                    .email(email)
                    .password(Password.fromRaw(password, passwordEncoder))
                    .gender(gender)
                    .birthDate(birthDate)
                    .authority(Authority.ROLE_USER)
                    .build();
        }
    }
}
