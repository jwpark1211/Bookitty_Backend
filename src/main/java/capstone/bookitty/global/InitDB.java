package capstone.bookitty.global;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.domain.type.Authority;
import capstone.bookitty.domain.member.domain.type.Gender;
import capstone.bookitty.domain.member.domain.vo.Password;
import capstone.bookitty.domain.star.domain.Star;
import capstone.bookitty.global.authentication.PasswordEncoder;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
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
        private final Random random = new Random(42); // 시드 고정으로 일관된 데이터

        public void dbInit() {
            log.info("🔥 대용량 테스트 데이터 생성 시작...");

            // 회원 데이터 (200명으로 대폭 증가)
            List<Member> members = createMembers();
            log.info("👥 회원 데이터 생성 중... ({} 명)", members.size());
            for (Member member : members) {
                em.persist(member);
            }
            em.flush();
            em.clear(); // 메모리 정리

            // 책 평점 데이터 (약 10만 건)
            log.info("📚 Star 데이터 생성 중... (목표: 약 10만 건)");
            createAndPersistStarRatings();

            log.info("✅ 대용량 테스트 데이터 생성 완료!");
        }

        private List<Member> createMembers() {
            List<Member> members = new ArrayList<>();

            // 기존 50명 + 추가 150명 = 총 200명
            String[] firstNames = {
                    "민준", "서연", "도윤", "하은", "시우", "아린", "준우", "지우", "준서", "유나",
                    "민서", "지호", "소율", "도현", "예준", "채원", "시윤", "서진", "하준", "지윤",
                    "현우", "지민", "건우", "나은", "승현", "다은", "준혁", "수아", "이준", "서우",
                    "태윤", "하린", "민재", "유진", "정우", "채은", "서준", "예은", "하온", "윤서",
                    "재윤", "소은", "도훈", "예린", "시현", "채윤", "민우", "서은", "준영", "다인"
            };

            String[] lastNames = {
                    "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
                    "한", "오", "서", "신", "권", "황", "안", "송", "류", "전",
                    "홍", "고", "문", "양", "손", "배", "조", "백", "허", "유",
                    "남", "심", "노", "정", "하", "곽", "성", "차", "주", "우"
            };

            // 200명의 회원 생성
            for (int i = 0; i < 200; i++) {
                String lastName = lastNames[random.nextInt(lastNames.length)];
                String firstName = firstNames[random.nextInt(firstNames.length)];
                String name = lastName + firstName;

                String email = generateEmail(name, i);
                Gender gender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
                LocalDate birthDate = generateRandomBirthDate();

                members.add(createUser(name, email, "Wo1902!si1", gender, birthDate));
            }

            return members;
        }

        private void createAndPersistStarRatings() {
            Set<String> existingRatings = new HashSet<>();
            int batchSize = 1000;
            int totalCreated = 0;
            int targetCount = 30000; // 목표 3만 건

            // 200권의 책 ISBN (더 많은 책으로 유사도 계산 의미 있게)
            String[] bookIsbns = generateBookIsbns(200);

            log.info("📖 {} 권의 책에 대한 평점 데이터 생성", bookIsbns.length);

            // 각 책마다 다양한 평점 수와 패턴 적용
            for (int bookIndex = 0; bookIndex < bookIsbns.length && totalCreated < targetCount; bookIndex++) {
                String isbn = bookIsbns[bookIndex];
                RatingPattern pattern = getRatingPattern(bookIndex);

                // 각 책마다 30-300명이 평가 (편차 큰 분포)
                int ratingCount = generateRatingCount(bookIndex);

                List<Long> selectedMembers = getRandomMembers(ratingCount);
                List<Star> currentBatch = new ArrayList<>();

                for (Long memberId : selectedMembers) {
                    String ratingKey = memberId + "_" + isbn;

                    if (existingRatings.contains(ratingKey)) {
                        continue;
                    }

                    double score = generateScore(pattern, random);
                    Star star = createStar(memberId, isbn, score);
                    currentBatch.add(star);
                    existingRatings.add(ratingKey);
                    totalCreated++;

                    // 배치로 저장 (메모리 효율성)
                    if (currentBatch.size() >= batchSize) {
                        persistStarBatch(currentBatch);
                        currentBatch.clear();

                        if (totalCreated % 10000 == 0) {
                            log.info("📊 Star 데이터 진행률: {} / {} ({:.1f}%)",
                                    totalCreated, targetCount, (double) totalCreated / targetCount * 100);
                        }
                    }

                    if (totalCreated >= targetCount) {
                        break;
                    }
                }

                // 남은 배치 저장
                if (!currentBatch.isEmpty()) {
                    persistStarBatch(currentBatch);
                }
            }

            log.info("⭐ 총 {} 건의 Star 데이터 생성 완료", totalCreated);
        }

        private void persistStarBatch(List<Star> stars) {
            for (Star star : stars) {
                em.persist(star);
            }
            em.flush();
            em.clear(); // 메모리 정리
        }

        private String[] generateBookIsbns(int count) {
            String[] isbns = new String[count];
            Set<String> used = new HashSet<>();

            for (int i = 0; i < count; i++) {
                String isbn;
                do {
                    // 실제 ISBN 형식으로 생성 (978로 시작)
                    isbn = "978" + String.format("%010d", random.nextInt(1000000000));
                } while (used.contains(isbn));

                isbns[i] = isbn;
                used.add(isbn);
            }

            return isbns;
        }

        private int generateRatingCount(int bookIndex) {
            // 인기 분포를 현실적으로 만들기 (CROSS JOIN 고려해서 조정)
            if (bookIndex % 20 == 0) {
                return 80 + random.nextInt(71); // 베스트셀러: 80-150 평점
            } else if (bookIndex % 10 == 0) {
                return 50 + random.nextInt(51); // 인기작: 50-100 평점
            } else if (bookIndex % 5 == 0) {
                return 30 + random.nextInt(31); // 중간: 30-60 평점
            } else {
                return 15 + random.nextInt(26); // 일반: 15-40 평점
            }
        }

        private List<Long> getRandomMembers(int count) {
            List<Long> members = new ArrayList<>();
            Set<Long> used = new HashSet<>();

            while (members.size() < count && used.size() < 200) {
                Long memberId = (long) random.nextInt(200); // 200명 중에서 선택
                if (!used.contains(memberId)) {
                    members.add(memberId);
                    used.add(memberId);
                }
            }
            return members;
        }

        private RatingPattern getRatingPattern(int bookIndex) {
            // 더 극단적이고 다양한 패턴들
            RatingPattern[] patterns = {
                    RatingPattern.MASTERPIECE,      // 거의 모든 평점이 4.5-5.0 (명작)
                    RatingPattern.HIGHLY_PRAISED,   // 4.0-5.0 중심이지만 가끔 낮은 점수
                    RatingPattern.ABOVE_AVERAGE,    // 3.5-4.5 중심
                    RatingPattern.AVERAGE,          // 2.5-4.0 골고루
                    RatingPattern.BELOW_AVERAGE,    // 2.0-3.5 중심
                    RatingPattern.POORLY_RECEIVED,  // 1.0-2.5 중심이지만 가끔 높은 점수
                    RatingPattern.TERRIBLE,         // 거의 모든 평점이 0.5-2.0 (혹평)
                    RatingPattern.EXTREMELY_POLARIZED, // 0.5-1.5 또는 4.5-5.0만 존재 (극도로 호불호)
                    RatingPattern.MILDLY_POLARIZED,    // 1.5-2.5 또는 3.5-4.5 (온건한 호불호)
                    RatingPattern.CULT_CLASSIC,        // 주로 낮지만 일부 극찬 (컬트)
                    RatingPattern.OVERHYPED,          // 기대에 못 미침 (높은 점수 많지만 낮은 점수도)
                    RatingPattern.RANDOM_CHAOS        // 완전 무작위
            };
            return patterns[bookIndex % patterns.length];
        }

        private double generateScore(RatingPattern pattern, Random rand) {
            double[] allScores = {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};

            return switch (pattern) {
                case MASTERPIECE -> {
                    // 95% 확률로 4.5-5.0, 5% 확률로 3.0-4.0 (소수 비판)
                    if (rand.nextDouble() < 0.95) {
                        yield allScores[8 + rand.nextInt(2)]; // 4.5-5.0
                    } else {
                        yield allScores[5 + rand.nextInt(3)]; // 3.0-4.0
                    }
                }
                case HIGHLY_PRAISED -> {
                    // 80% 확률로 4.0-5.0, 20% 확률로 나머지
                    if (rand.nextDouble() < 0.8) {
                        yield allScores[7 + rand.nextInt(3)]; // 4.0-5.0
                    } else {
                        yield allScores[rand.nextInt(7)]; // 0.5-3.5
                    }
                }
                case ABOVE_AVERAGE -> {
                    // 3.5-4.5 중심이지만 양쪽 극단값도 소량
                    if (rand.nextDouble() < 0.7) {
                        yield allScores[6 + rand.nextInt(3)]; // 3.5-4.5
                    } else {
                        yield allScores[rand.nextInt(allScores.length)];
                    }
                }
                case AVERAGE -> {
                    // 2.5-4.0 중심
                    if (rand.nextDouble() < 0.8) {
                        yield allScores[4 + rand.nextInt(4)]; // 2.5-4.0
                    } else {
                        yield allScores[rand.nextInt(allScores.length)];
                    }
                }
                case BELOW_AVERAGE -> {
                    // 2.0-3.5 중심
                    if (rand.nextDouble() < 0.8) {
                        yield allScores[3 + rand.nextInt(4)]; // 2.0-3.5
                    } else {
                        yield allScores[rand.nextInt(allScores.length)];
                    }
                }
                case POORLY_RECEIVED -> {
                    // 80% 확률로 1.0-2.5, 20% 확률로 나머지 (일부 옹호)
                    if (rand.nextDouble() < 0.8) {
                        yield allScores[1 + rand.nextInt(4)]; // 1.0-2.5
                    } else {
                        yield allScores[5 + rand.nextInt(5)]; // 3.0-5.0
                    }
                }
                case TERRIBLE -> {
                    // 95% 확률로 0.5-2.0, 5% 확률로 2.5-3.5 (극소수 변호)
                    if (rand.nextDouble() < 0.95) {
                        yield allScores[rand.nextInt(4)]; // 0.5-2.0
                    } else {
                        yield allScores[4 + rand.nextInt(3)]; // 2.5-3.5
                    }
                }
                case EXTREMELY_POLARIZED -> {
                    // 극도로 갈림: 50% 확률로 0.5-1.5, 50% 확률로 4.5-5.0
                    if (rand.nextBoolean()) {
                        yield allScores[rand.nextInt(3)]; // 0.5-1.5
                    } else {
                        yield allScores[8 + rand.nextInt(2)]; // 4.5-5.0
                    }
                }
                case MILDLY_POLARIZED -> {
                    // 온건한 갈림: 40% 1.5-2.5, 40% 3.5-4.5, 20% 나머지
                    double r = rand.nextDouble();
                    if (r < 0.4) {
                        yield allScores[2 + rand.nextInt(3)]; // 1.5-2.5
                    } else if (r < 0.8) {
                        yield allScores[6 + rand.nextInt(3)]; // 3.5-4.5
                    } else {
                        yield allScores[rand.nextInt(allScores.length)];
                    }
                }
                case CULT_CLASSIC -> {
                    // 70% 확률로 1.5-3.0, 30% 확률로 4.5-5.0 (컬트적 인기)
                    if (rand.nextDouble() < 0.7) {
                        yield allScores[2 + rand.nextInt(4)]; // 1.5-3.0
                    } else {
                        yield allScores[8 + rand.nextInt(2)]; // 4.5-5.0
                    }
                }
                case OVERHYPED -> {
                    // 기대에 못 미침: 60% 확률로 2.0-3.5, 40% 확률로 4.0-5.0
                    if (rand.nextDouble() < 0.6) {
                        yield allScores[3 + rand.nextInt(4)]; // 2.0-3.5
                    } else {
                        yield allScores[7 + rand.nextInt(3)]; // 4.0-5.0
                    }
                }
                case RANDOM_CHAOS -> {
                    // 완전 무작위
                    yield allScores[rand.nextInt(allScores.length)];
                }
            };
        }

        private enum RatingPattern {
            MASTERPIECE,           // 거의 만점 (명작)
            HIGHLY_PRAISED,        // 높은 평가
            ABOVE_AVERAGE,         // 평균 이상
            AVERAGE,               // 평균적
            BELOW_AVERAGE,         // 평균 이하
            POORLY_RECEIVED,       // 낮은 평가
            TERRIBLE,              // 거의 최저점 (혹평)
            EXTREMELY_POLARIZED,   // 극도로 호불호 갈림
            MILDLY_POLARIZED,      // 온건하게 호불호 갈림
            CULT_CLASSIC,          // 컬트적 인기 (소수가 극찬)
            OVERHYPED,             // 과대평가 후 실망
            RANDOM_CHAOS           // 완전 무작위
        }

        private String generateEmail(String name, int index) {
            // 한글 이름을 영어로 변환
            String englishName = convertKoreanToEnglish(name);
            String[] domains = {"gmail.com", "naver.com", "daum.net", "yahoo.com", "hotmail.com"};
            String domain = domains[random.nextInt(domains.length)];
            return englishName.toLowerCase() + index + "@" + domain;
        }

        private String convertKoreanToEnglish(String koreanName) {
            // 한글 이름 → 영어 이름 매핑
            Map<String, String> nameMap = new HashMap<>();

            // 성씨 매핑
            nameMap.put("김", "kim");
            nameMap.put("이", "lee");
            nameMap.put("박", "park");
            nameMap.put("최", "choi");
            nameMap.put("정", "jung");
            nameMap.put("강", "kang");
            nameMap.put("조", "cho");
            nameMap.put("윤", "yoon");
            nameMap.put("장", "jang");
            nameMap.put("임", "lim");
            nameMap.put("한", "han");
            nameMap.put("오", "oh");
            nameMap.put("서", "seo");
            nameMap.put("신", "shin");
            nameMap.put("권", "kwon");
            nameMap.put("황", "hwang");
            nameMap.put("안", "ahn");
            nameMap.put("송", "song");
            nameMap.put("류", "ryu");
            nameMap.put("전", "jeon");
            nameMap.put("홍", "hong");
            nameMap.put("고", "go");
            nameMap.put("문", "moon");
            nameMap.put("양", "yang");
            nameMap.put("손", "son");
            nameMap.put("배", "bae");
            nameMap.put("백", "baek");
            nameMap.put("허", "heo");
            nameMap.put("유", "yu");
            nameMap.put("남", "nam");
            nameMap.put("심", "sim");
            nameMap.put("노", "noh");
            nameMap.put("하", "ha");
            nameMap.put("곽", "kwak");
            nameMap.put("성", "sung");
            nameMap.put("차", "cha");
            nameMap.put("주", "ju");
            nameMap.put("우", "woo");

            // 이름 매핑
            nameMap.put("민준", "minjun");
            nameMap.put("서연", "seoyeon");
            nameMap.put("도윤", "doyun");
            nameMap.put("하은", "haeun");
            nameMap.put("시우", "siwoo");
            nameMap.put("아린", "arin");
            nameMap.put("준우", "junwoo");
            nameMap.put("지우", "jiwoo");
            nameMap.put("준서", "junseo");
            nameMap.put("유나", "yuna");
            nameMap.put("민서", "minseo");
            nameMap.put("지호", "jiho");
            nameMap.put("소율", "soyul");
            nameMap.put("도현", "dohyun");
            nameMap.put("예준", "yejun");
            nameMap.put("채원", "chaewon");
            nameMap.put("시윤", "siyun");
            nameMap.put("서진", "seojin");
            nameMap.put("하준", "hajun");
            nameMap.put("지윤", "jiyun");
            nameMap.put("현우", "hyunwoo");
            nameMap.put("지민", "jimin");
            nameMap.put("건우", "gunwoo");
            nameMap.put("나은", "naeun");
            nameMap.put("승현", "seunghyun");
            nameMap.put("다은", "daeun");
            nameMap.put("준혁", "junhyuk");
            nameMap.put("수아", "sua");
            nameMap.put("이준", "ijun");
            nameMap.put("서우", "seowoo");
            nameMap.put("태윤", "taeyun");
            nameMap.put("하린", "harin");
            nameMap.put("민재", "minjae");
            nameMap.put("유진", "yujin");
            nameMap.put("정우", "jungwoo");
            nameMap.put("채은", "chaeeun");
            nameMap.put("서준", "seojun");
            nameMap.put("예은", "yeeun");
            nameMap.put("하온", "haon");
            nameMap.put("윤서", "yunseo");
            nameMap.put("재윤", "jaeyun");
            nameMap.put("소은", "soeun");
            nameMap.put("도훈", "dohun");
            nameMap.put("예린", "yerin");
            nameMap.put("시현", "sihyun");
            nameMap.put("채윤", "chaeyun");
            nameMap.put("민우", "minwoo");
            nameMap.put("서은", "seoeun");
            nameMap.put("준영", "junyoung");
            nameMap.put("다인", "dain");

            // 이름을 성과 이름으로 분리
            String lastName = koreanName.substring(0, 1); // 첫 글자는 성
            String firstName = koreanName.substring(1);   // 나머지는 이름

            String englishLastName = nameMap.getOrDefault(lastName, "kim");
            String englishFirstName = nameMap.getOrDefault(firstName, "unknown");

            return englishLastName + englishFirstName;
        }

        private LocalDate generateRandomBirthDate() {
            int year = 1970 + random.nextInt(35); // 1970-2004
            int month = 1 + random.nextInt(12);
            int day = 1 + random.nextInt(28); // 안전하게 28일까지
            return LocalDate.of(year, month, day);
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