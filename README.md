## 📚 북키티 (Bookitty) : 사용자 맞춤형 도서 추천 서비스

<img src="https://github.com/user-attachments/assets/f2b1a69b-1551-41d9-bcb1-c74702a6b28d" width="700"/>

<br>
<br>

## 🧭 Intro


- **AI 기반 도서 추천 플랫폼**으로, 사용자 평점 데이터를 바탕으로 **코사인 유사도(Cosine Similarity)** 알고리즘을 적용한 **개인화 추천 시스템**을 구현하였습니다.
<img src="https://github.com/user-attachments/assets/a6f107a5-0cf2-49f5-8409-285c0aa4edc9" width="400"/>

- 추천 벡터는 **Spring Batch 기반의 배치 전용 모듈**을 통해 주기적으로 계산 및 저장되며, 실시간 서비스와의 역할을 분리하여 **성능 최적화와 시스템 확장성**을 고려한 백엔드 아키텍처를 설계했습니다.
<img width="300" alt="image" src="https://github.com/user-attachments/assets/cb91a9d7-bdbd-4689-acec-296dcc7f0b2a" />

- 개인 맞춤형 도서 외에도 **베스트셀러, 신간, 장르별 인기 도서 추천** 기능을 제공하며, 사용자 간 소통(댓글, 피드 등)을 통해 **독서 경험을 공유할 수 있는 커뮤니케이션 기능**도 함께 구현하였습니다.

<br>
<br>


## ✨ Feature Highlights

- 사용자의 **평점 기반 데이터를 분석**하여 취향에 부합하는 도서를 추천
  - 개인 맞춤 도서 추천 (코사인 유사도 기반)
  - 전체 베스트셀러
  - 신간 베스트셀러
  - 카테고리별 베스트셀러 (경제경영 / 인문 / 문학 / 과학 / 자기계발)
- 도서 검색 기능 (저자, 출판사, 제목 기준)
- 도서를 기록하고, 의견을 나눌 수 있는 **커뮤니케이션 공간**
- 월별 / 연도별 / 카테고리별 **사용자 독서 통계** 제공

<br>
<br>

### Backend
- Java, Spring Boot
- Spring Security, Spring Batch, Spring WebFlux
- Spring Data JPA, QueryDSL

### Database
- MySQL
- Redis

### Testing
- JUnit
- Mockito
- Apache JMeter

### Deployment & DevOps
- Docker
- AWS
- GitHub Actions
- Swagger

### Monitoring
- Prometheus
- Grafana

<br>
<br>

## 🧩 ERD

<img src="https://github.com/user-attachments/assets/51157f25-f1f5-4c27-8a69-295105b76835" width="700"/>

<br>
<br>

## 📌 Project Info

- 👤 **Team**: 개인 프로젝트 (1인 개발)
- 🗓️ **Period**: 2024.03 ~ 

<br>
<br>

## 📁 Project Structure
<pre>
📦 capstone.bookitty  
├── 📁 domain                          
│       ├── 📁 book                        
│       ├── 📁 bookSimilarity              
│       ├── 📁 bookState                   
│       ├── 📁 comment                     
│       ├── 📁 star                        
│       └── 📁 member                      
│             ├── 📁 api                     
│             ├── 📁 application            
│             ├── 📁 domain                 
│             ├── 📁 dto                    
│             ├── 📁 exception              
│             └── 📁 repository             
│
├── 📁 global                          
│    ├── 📁 annotation                  
│    ├── 📁 application                
│    ├── 📁 authentication             
│    ├── 📁 config                     
│    ├── 📁 dto                        
│    ├── 📁 error                      
│    └── 📁 util                       
│
└── 📄 BookittyApplication.java
