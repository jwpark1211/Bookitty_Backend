// InitDB.java - 확장된 더미 데이터
package capstone.bookitty.global;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.type.Authority;
import capstone.bookitty.domain.member.domain.type.Gender;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.global.authentication.PasswordEncoder;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class InitDB {
    private final InitService initService;


    @PostConstruct
    public void init() {
        initService.dbInit();
    }

    @Component
    @Transactional("dataTransactionManager")
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;
        private final PasswordEncoder passwordEncoder;
        private final Random random = new Random(42); // 시드 고정으로 일관된 데이터

        public void dbInit() {
            // 회원 데이터 (30명으로 증가)
            List<Member> members = createMembers();
            for (Member member : members) {
                em.persist(member);
            }

            // 책 평점 데이터 (50권 + 500+ 평점)
            List<Star> stars = createStarRatings();
            for (Star star : stars) {
                em.persist(star);
            }
        }

        private List<Member> createMembers() {
            return Arrays.asList(
                    // 기존 회원 16명
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
                    createUser("이예진", "dldPwls@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2001, 2, 16)),

                    // 추가 회원 34명 (총 50명)
                    createUser("박지훈", "wlgns@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1994, 6, 15)),
                    createUser("최유나", "dbssk@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(1996, 11, 3)),
                    createUser("정재민", "wowalsals@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1988, 4, 22)),
                    createUser("한소영", "gktyddud@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2003, 9, 17)),
                    createUser("오태현", "dhxogus@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1993, 12, 7)),
                    createUser("배서연", "qotjdus@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(1997, 5, 25)),
                    createUser("임태윤", "dlfxodbsd@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1990, 10, 12)),
                    createUser("강민서", "rkdalstp@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2006, 1, 30)),
                    createUser("홍성빈", "ghdrhtqls@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1985, 8, 14)),
                    createUser("신채원", "tlscowhs@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2004, 3, 8)),
                    createUser("류승호", "fbdtmdgh@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1987, 7, 19)),
                    createUser("김하은", "gkgkdms@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2000, 4, 11)),
                    createUser("노준혁", "shwnsgur@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1998, 9, 5)),
                    createUser("이다은", "ekekdms@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2007, 6, 23)),
                    createUser("조현수", "whgustngmd@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1991, 5, 18)),
                    createUser("윤서은", "dbstjdms@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2002, 8, 9)),
                    createUser("장민혁", "wkdalsrud@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1989, 11, 14)),
                    createUser("김수빈", "rlatkqls@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2005, 12, 1)),
                    createUser("박도현", "ehrgus@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1996, 2, 27)),
                    createUser("이유진", "dldnwls@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2001, 7, 16)),
                    createUser("정시우", "wjdtldn@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1993, 10, 22)),
                    createUser("한예원", "gksdPwls@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2008, 4, 5)),
                    createUser("김태민", "xoalsals@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1990, 6, 30)),
                    createUser("송아름", "thddmsaaa@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2004, 9, 12)),
                    createUser("이동욱", "ekddnrdnr@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1987, 1, 8)),
                    createUser("박소희", "qkrtmgml@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2003, 5, 21)),
                    createUser("최재혁", "wowlsgud@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1995, 8, 17)),
                    createUser("강유진", "rkddnwls@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2006, 11, 4)),
                    createUser("임현준", "dlfguswnss@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1992, 3, 13)),
                    createUser("신예은", "tlsdPdms@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2007, 12, 25)),
                    createUser("홍준호", "ghdrwnsghs@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1988, 9, 19)),
                    createUser("윤하늘", "dbsgksmf@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2005, 2, 7)),
                    createUser("김성민", "rlaturals@gmail.com", "Wo1902!si1", Gender.MALE, LocalDate.of(1991, 7, 24)),
                    createUser("이채영", "dlcodud@gmail.com", "Wo1902!si1", Gender.FEMALE, LocalDate.of(2002, 10, 15))
            );
        }

        private List<Star> createStarRatings() {
            List<Star> stars = new ArrayList<>();
            Set<String> existingRatings = new HashSet<>(); // 중복 체크용

            // 100권의 책 ISBN (실제 ISBN 형식)
            String[] bookIsbns = {
                    // 소설/문학 (1-25)
                    "9788952776372", "9791189327156", "9791198686114", "9788954697941", "9788936425012",
                    "9788962622706", "9791141020866", "9791169092203", "9788998441012", "9791138483049",
                    "9791165653330", "9791130646381", "9791193506530", "9791130698199", "9788937462788",
                    "9788932917245", "9788954439695", "9791197377310", "9788965746267", "9791130635232",
                    "9788954676397", "9791165219567", "9788937473302", "9791165810344", "9791160946789",

                    // 인문/사회과학 (26-50)
                    "9788983711892", "9788954429184", "9791193317656", "9791191043426", "9788965133445",
                    "9791197652769", "9788954622998", "9791165345897", "9788965962435", "9791191043327",
                    "9788932919867", "9791189327613", "9788983926456", "9791165219789", "9788937460234",
                    "9791198511567", "9788954439723", "9791130635156", "9788965133378", "9791191043289",
                    "9788932917123", "9791189327445", "9788983926332", "9791165219623", "9788937460178",

                    // 과학/기술 (51-75)
                    "9788932916234", "9791189327987", "9788983926789", "9791165219456", "9788937460567",
                    "9791198511234", "9788954439567", "9791130635789", "9788965133567", "9791191043567",
                    "9788932917567", "9791189327234", "9788983926123", "9791165219234", "9788937460345",
                    "9791198511345", "9788954439345", "9791130635345", "9788965133345", "9791191043345",
                    "9788932917345", "9791189327345", "9788983926345", "9791165219345", "9788937460123",

                    // 자기계발/경제 (76-100)
                    "9791198511456", "9788954439456", "9791130635456", "9788965133456", "9791191043456",
                    "9788932917456", "9791189327456", "9788983926456", "9791165219456", "9788937460456",
                    "9791198511789", "9788954439789", "9791130635789", "9788965133789", "9791191043789",
                    "9788932917789", "9791189327789", "9788983926789", "9791165219789", "9788937460789",
                    "9791198511012", "9788954439012", "9791130635012", "9788965133012", "9791191043012"
            };

            // 다양한 평점 패턴 생성 - 시드를 다르게 해서 더 랜덤하게
            Random patternRandom = new Random(System.currentTimeMillis()); // 매번 다른 시드

            for (int bookIndex = 0; bookIndex < bookIsbns.length; bookIndex++) {
                String isbn = bookIsbns[bookIndex];
                RatingPattern pattern = getRatingPattern(bookIndex);

                // 각 책마다 5-20명이 평가 (범위 조정)
                int ratingCount = 5 + patternRandom.nextInt(16);
                List<Long> selectedMembers = getRandomMembers(ratingCount);

                for (Long memberId : selectedMembers) {
                    String ratingKey = memberId + "_" + isbn;

                    // 중복 체크: 이미 이 회원이 이 책을 평가했으면 건너뛰기
                    if (existingRatings.contains(ratingKey)) {
                        continue;
                    }

                    double score = generateScore(pattern);
                    stars.add(createStar(memberId, isbn, score));
                    existingRatings.add(ratingKey); // 중복 방지용 Set에 추가
                }
            }

            return stars;
        }

        private List<Long> getRandomMembers(int count) {
            List<Long> members = new ArrayList<>();
            while (members.size() < count) {
                Long memberId = (long) random.nextInt(50); // 50명 중에서 선택
                if (!members.contains(memberId)) {
                    members.add(memberId);
                }
            }
            return members;
        }

        private RatingPattern getRatingPattern(int bookIndex) {
            RatingPattern[] patterns = {
                    RatingPattern.HIGH_RATINGS,    // 4-5점 중심
                    RatingPattern.MEDIUM_HIGH,     // 3.5-4.5점 중심
                    RatingPattern.MEDIUM,          // 3-4점 중심
                    RatingPattern.MEDIUM_LOW,      // 2.5-3.5점 중심
                    RatingPattern.LOW_RATINGS,     // 1-3점 중심
                    RatingPattern.MIXED,           // 전체 범위 섞임
                    RatingPattern.POLARIZED,       // 1-2점 또는 4-5점
                    RatingPattern.CONSISTENT       // 특정 점수 집중
            };
            return patterns[bookIndex % patterns.length];
        }

        private double generateScore(RatingPattern pattern) {
            double[] possibleScores = {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};

            return switch (pattern) {
                case HIGH_RATINGS -> {
                    // 4-5점 중심이지만 가끔 낮은 점수도 (70% 높은 점수)
                    if (random.nextDouble() < 0.7) {
                        yield possibleScores[6 + random.nextInt(4)]; // 3.5-5.0
                    } else {
                        yield possibleScores[random.nextInt(6)]; // 0.5-3.0 (이상치)
                    }
                }
                case MEDIUM_HIGH -> {
                    // 3.5-4.5점 중심이지만 더 넓은 분포
                    if (random.nextDouble() < 0.6) {
                        yield possibleScores[6 + random.nextInt(3)]; // 3.5-4.5
                    } else {
                        yield possibleScores[2 + random.nextInt(8)]; // 1.5-5.0
                    }
                }
                case MEDIUM -> {
                    // 진짜 랜덤 (전체 범위)
                    yield possibleScores[random.nextInt(possibleScores.length)];
                }
                case MEDIUM_LOW -> {
                    // 2-3점 중심이지만 극단값도 포함
                    if (random.nextDouble() < 0.6) {
                        yield possibleScores[3 + random.nextInt(3)]; // 2.0-3.5
                    } else {
                        yield possibleScores[random.nextInt(possibleScores.length)]; // 전체
                    }
                }
                case LOW_RATINGS -> {
                    // 1-2점 중심이지만 가끔 높은 점수도 (호불호 갈림)
                    if (random.nextDouble() < 0.7) {
                        yield possibleScores[1 + random.nextInt(3)]; // 1.0-2.0
                    } else {
                        yield possibleScores[7 + random.nextInt(3)]; // 4.0-5.0 (이상치)
                    }
                }
                case MIXED -> {
                    // 완전 랜덤
                    yield possibleScores[random.nextInt(possibleScores.length)];
                }
                case POLARIZED -> {
                    // 극단적 양극화: 1점 이하 또는 4.5점 이상
                    if (random.nextBoolean()) {
                        yield possibleScores[random.nextInt(3)]; // 0.5-1.5
                    } else {
                        yield possibleScores[8 + random.nextInt(2)]; // 4.5-5.0
                    }
                }
                case CONSISTENT -> {
                    // 한 점수에 집중하되 ±1점 범위에서 변동
                    double[] concentratedScores = {1.0, 2.0, 3.0, 4.0, 5.0};
                    double baseScore = concentratedScores[random.nextInt(concentratedScores.length)];

                    // 기준점 ±1.0 범위에서 랜덤
                    double variation = (random.nextDouble() - 0.5) * 2.0; // -1.0 ~ 1.0
                    double finalScore = Math.max(0.5, Math.min(5.0, baseScore + variation));

                    // 0.5 단위로 반올림
                    yield Math.round(finalScore * 2.0) / 2.0;
                }
            };
        }

        private int findClosestScoreIndex(double[] scores, double target) {
            int closest = 0;
            double minDiff = Math.abs(scores[0] - target);
            for (int i = 1; i < scores.length; i++) {
                double diff = Math.abs(scores[i] - target);
                if (diff < minDiff) {
                    minDiff = diff;
                    closest = i;
                }
            }
            return closest;
        }

        private enum RatingPattern {
            HIGH_RATINGS,    // 높은 점수 집중
            MEDIUM_HIGH,     // 중상 점수
            MEDIUM,          // 중간 점수
            MEDIUM_LOW,      // 중하 점수
            LOW_RATINGS,     // 낮은 점수 집중
            MIXED,           // 다양한 점수 분포
            POLARIZED,       // 양극화된 점수
            CONSISTENT       // 일관된 점수
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

        public Star createStar(Long memberId, String isbn, double score) {
            return Star.builder()
                    .memberId(memberId)
                    .isbn(isbn)
                    .score(score)
                    .build();
        }
    }
}
