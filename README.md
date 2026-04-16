## 실행 방법

### 1. 일반 실행 (소량 데이터)
- 옵션 없이 바로 실행 가능
- 기본 실행 환경: `dev`

<br>

### 2. 대용량 데이터 실행 (성능 테스트)
- 실행 옵션: 
```text
--spring.profiles.active=load
```

※ load 환경에서는 Java 기반으로 대량 데이터가 생성됩니다. <br>
※ 메모리 DB(H2) 기반으로 빠른 실행을 위해 <br> &nbsp;&nbsp;&nbsp; 게시글/회원 데이터 각각 10만, 좋아요 2만 데이터로 구성했습니다.

<br>

---

<br>

## API 테스트 방법
### 1. 게시글 최신순 조회
GET
```url
http://localhost:8080/api/dev/boards?pageIndex=0&sortType=latest
```

<br>

### 2. 게시글 최신 100건 기준 좋아요 수 정렬 조회
GET
```url
http://localhost:8080/api/dev/boards?pageIndex=0&sortType=latest_top_n_like_count
```

→ JSON 응답으로 바로 결과 확인 가능

<br>

※ 개발 및 테스트 편의를 위해 `/api/dev/**` 경로는 인증 없이 호출 가능하도록 구성했습니다.

<br>

---

<br>

## 브라우저 테스트 방법 (UX 확인)
### 1. Mock 로그인
GET
```url
http://localhost:8080/dev/login-test
```

→ 로그인 상태 생성 후 게시글 목록으로 리다이렉트

<br>

### 2. 게시글 화면 확인
GET
```url
http://localhost:8080/boards
```

<br>

---

<br>

## H2 콘솔 접속
- URL: 
```url
http://localhost:8080/h2-console
```

- JDBC URL: 
```url
jdbc:h2:mem:testdb
```

- Username: `sa`
- Password: (공백)

<br>

---

<br>

(자세히 보기↓↓)

<details>
<summary><h2>1. 부하 테스트 기반 게시판 성능 개선 (k6 + 인덱스 최적화)</h2></summary>

### [부하 테스트 환경]

#### 인프라 구조

![k6 부하 테스트 인프라 구조](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/k6%20%EB%B6%80%ED%95%98%20%ED%85%8C%EC%8A%A4%ED%8A%B8%20%EC%9D%B8%ED%94%84%EB%9D%BC%20%EA%B5%AC%EC%A1%B0.png)

#### 데이터
- 게시판/ 회원 테이블 데이터 각각 100만
- 게시판 삭제된 수 10만 (Sofe Delete → deleted_at 컬럼 NULL 유무 판단)
- 좋아요 테이블 데이터 20만

<br>

#### 시나리오
- 10분간 최대 50 가상 유저로 부하 테스트 수행
- 로그인 후 게시글 조회 95%, 작성 5%

<br>

#### 부하 테스트 진행한 API
- 게시글 작성 최신순 조회
- 페이징 처리 → 한 페이지당 10개 게시글 조회

<br>

#### 게시글 최신순 조회 쿼리(메인 쿼리)
```sql
SELECT
	b.board_id,
	b.title,
	b.view_count,
	b.created_date,
	m.name 
FROM 
	board b 
JOIN 
	member m ON m.member_id = b.member_id 
WHERE 
	b.deleted_at IS NULL
ORDER BY 
	b.created_date DESC,
	b.board_id
LIMIT 0,10;
```

<br>

#### COUNT 쿼리
```sql
SELECT 
	COUNT(b.board_id) 
FROM 
	board b 
WHERE 
	b.deleted_at IS NULL;
```

<br>

### [부하 테스트 기반 문제 발견]
#### k6 대시보드 Latency 평균 수치
![k6 대시보드 Latency 평균 수치](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/K6%20Latency(%EA%B0%9C%EC%84%A0%20%EC%A0%84).png)
- Latency: **평균 12초**

<br>

#### k6 대시보드 RPS, Request Failed 수치
![k6 대시보드 RPS, Request Failed 수치](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/k6%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%A0%84%20RPS%2C%20Request%20Failed(%EA%B0%9C%EC%84%A0%20%EC%A0%84).png)
- RPS: **1.84/s**
- Request Failed: **17.5%**

<br>

#### AWS CloudWatch 대시보드
![AWS CloudWatch EC2 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/EC2%20-%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0(%EA%B0%9C%EC%84%A0%20%EC%A0%84).png)
- EC2 서버 2대
    - CPU 사용률 **3.76% 이하**
    - Memory 사용률 **59.2% 이하**

<br>

![AWS CloudWatch RDS 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/RDS%20-%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0(%EA%B0%9C%EC%84%A0%20%EC%A0%84).png)
- RDS
    - CPU 사용률 **35.6% 이하**
    - 사용 가능한 메모리양 **110.4M 이상**   

<br>

- EC2 / RDS의 CPU, Memory 사용률은 여유있음

<br>

#### k6 로그
![k6 로그](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/k6%20Request%20timeout(%EA%B0%9C%EC%84%A0%20%EC%A0%84).png)
- Request timeout으로 인한 Request Failed 발생

<br>

### [병목 원인 분석]
#### 1. HikariCP 커넥션 풀 확장 후 다시 테스트 진행
```YAML
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
```
- EC2  2대의 서버 각각 커넥션 풀 20으로 확장 → 총 커넥션 풀 40

<br>

#### k6 대시보드 Latency 평균 수치
![k6 대시보드 Latency 평균 수치](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/connection%20pool/k6%20%EC%BB%A4%EB%84%A5%EC%85%98%20%ED%92%80%20%ED%99%95%EC%9E%A5%20%ED%9B%84%20Latency.png)
- Latency: **평균 12초**

<br>

#### k6 대시보드 RPS, Request Failed 수치
![k6 대시보드 RPS, Request Failed 수치](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/connection%20pool/k6%20%EC%BB%A4%EB%84%A5%EC%85%98%20%ED%92%80%20%ED%99%95%EC%9E%A5%20%ED%9B%84%20RPS%2C%20Request%20Failed.png)
- RPS: **1.89/s**
- Request Failed: **22.9%**

<br>

#### AWS CloudWatch 대시보드
![AWS CloudWatch EC2 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/connection%20pool/EC2%20-%20%EC%BB%A4%EB%84%A5%EC%85%98%20%ED%92%80%20%ED%99%95%EC%9E%A5%20%ED%9B%84%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0.png)
- EC2 서버 2대
    - CPU 사용률 **3.58% 이하**
    - Memory 사용률 **57.9% 이하**

<br>

![AWS CloudWatch RDS 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/connection%20pool/RDS%20-%20%EC%BB%A4%EB%84%A5%EC%85%98%20%ED%92%80%20%ED%99%95%EC%9E%A5%20%ED%9B%84%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0.png)
- RDS
    - CPU 사용률 **36.5% 이하**
    - 사용 가능한 메모리양 **93.95M 이상**

<br>


- 동일하게 EC2 / RDS의 CPU, Memory 사용률은 여유있음

<br>

#### k6 로그
![k6 로그](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/k6%20Request%20timeout(%EA%B0%9C%EC%84%A0%20%EC%A0%84).png)
- 동일하게 Request timeout으로 인한 Request Failed 발생

<br>

#### 2. DB 쿼리 성능을 확인하기 위해 응답 소요 시간 확인

|          | 응답 소요 시간 |                                                                                                                                                   측정 시간                                                                                                                                                    |
|:--------:|:--------:|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|  메인 쿼리   |  약 2.8초  |                ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20%EA%B0%9C%EC%84%A0%20%EC%A0%84%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png)                |
| COUNT 쿼리 |  약 1.7초  | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%B9%B4%EC%9A%B4%ED%8A%B8%20%EC%BF%BC%EB%A6%AC%20%EA%B0%9C%EC%84%A0%20%EC%A0%84%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |

<br>

#### Explain Analyze를 통한 병목 확인
**<메인 쿼리>**
```sql
Table scan on b (actual time=0.767..481 rows=1e+6 loops=1)
``` 
약 100만 건(`rows=1e+6`) 풀테이블 스캔
- 소요 시간: **481ms → 약 0.5초**

<br>

```sql
Filter: (b.deleted_at is null) (actual time=0.768..558 rows=899962 loops=1)
```

- 정렬 전 WHERE절 조건 필터링까지 소요 시간: **558ms**
- 필터링 후 남은 데이터 수: `rows=899962` → **약 90만**

<br>

```sql
Sort: b.created_date DESC, b.board_id (actual time=2861..2861 rows=10 loops=1)
```

- 약 90만 건 정렬
- 정렬 소요 시간: 2861ms - 558ms = 2303ms → **약 2.3초**

<br>

**<COUNT 쿼리>**  
```sql
Table scan on b (actual time=2.16..1594 rows=1e+6 loops=1)
``` 
약 100만 건(`rows=1e+6`) 풀테이블 스캔
- 소요 시간: **1594ms → 약 1.6초**

<br>

- 메인 쿼리 주요 병목
  - 풀테이블 스캔: **약 0.5초**
  - 정렬: **약 2.3초**

- COUNT 쿼리 주요 병목
  - 풀테이블 스캔: **약 1.6초**

<br>

### [성능 개선 - 인덱스 설계]
#### 1. 단일 컬럼 인덱스 적용
```sql
CREATE INDEX idx_board_created_date_desc ON board (created_date DESC);
```

<br>

|     | 기존 응답 소요 시간  | 단일 컬럼 인덱스 적용 응답 소요 시간 |                                                                                                                                                                                 측정 시간                                                                                                                                                                                 |
|:---:|:------------:|:---------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| 메인 쿼리 | 약 2.8초 |       약 0.007초        |                ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20%EB%8B%A8%EC%9D%BC%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png)                |
| COUNT 쿼리 | 약 1.7초 |        약 1.7초         | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%B9%B4%EC%9A%B4%ED%8A%B8%20%EC%BF%BC%EB%A6%AC%20%EB%8B%A8%EC%9D%BC%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |

- 메인쿼리: **약 2.8초 → 약 0.007초 (약 400배 개선)**
- COUNT 쿼리: **차이 미미**

<br>

#### 메인 쿼리  

**\<Explain\>**  

|      | 인덱스 적용 전 |   인덱스 적용 후   |
|:----:|:--------:|:------------:|
| type |   ALL    |    index     |
|Extra | Using where; Using filesort | Using where  |

- **풀테이블 스캔 (ALL)** → **인덱스 스캔 (index)**
- **정렬 작업 제거 (Using filesort)**

<br>

**\<Explain Analyze\>**  
```sql
Index scan on b using idx_board_created_date_desc (actual time=0.055..0.098 rows=13 loops=1)
```
- 약 100만 건(`rows=1e+6`) 풀테이블 스캔 → `rows=13`으로 데이터 스캔 양 감소
- 소요 시간: **기존 481ms → 0.098ms**

<br>

#### COUNT 쿼리
- 실행 계획 **변화 없음**

<br>

#### 2. 멀티 컬럼 인덱스 적용  
```sql
CREATE INDEX idx_board_deleted_at_created_date_desc ON board (deleted_at, created_date DESC);
```

<br>

|     | 단일 컬럼 인덱스 적용 응답 소요 시간 | 멀티 컬럼 인덱스 적용 응답 소요 시간 |                                                                                                                                                                                 측정 시간                                                                                                                                                                                 |
|:---:|:---------------------:|:---------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| 메인 쿼리 |       약 0.007초        |       약 0.007초        |                ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20%EB%A9%80%ED%8B%B0%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png)                |
|COUNT 쿼리 |        약 1.7초         |        약 0.4초         | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%B9%B4%EC%9A%B4%ED%8A%B8%20%EC%BF%BC%EB%A6%AC%20%EB%A9%80%ED%8B%B0%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |


- 메인쿼리: **차이 미미**
- COUNT 쿼리: **약 1.7초 → 약 0.4초 (약 4배 개선)**

<br>

#### 메인 쿼리 
**\<Explain\>**  

|      | 단일 컬럼 인덱스 적용 | 멀티 컬럼 인덱스 적용 |
|:----:|:---:|:---:|
| type | index | ref |
| Extra | Using where | Using index condition |

- **인덱스 스캔 (index) → 인덱스 참조 스캔 (ref)**
- **인덱스 기반 필터링으로 변경 (Using index condition)**

<br>

**\<Explain Analyze\>**  
```sql
Index lookup on b using idx_board_deleted_at_created_date_desc 
(deleted_at=NULL), with index condition: (b.deleted_at is null) 
(actual time=0.0612..0.0892 rows=10 loops=1)
```
- 인덱스 기반 필터링으로 LIMIT 10 에 맞는 데이터만 조회 `rows=10`

<br>

#### COUNT 쿼리
**\<Explain\>**  

|      | 단일 컬럼 인덱스 적용 | 멀티 컬럼 인덱스 적용 |
|:----:|:---:|:---:|
| type | ALL | ref |
| Extra | Using where | Using where; Using index |

- **풀테이블 스캔 (ALL) → 인덱스 참조 스캔 (ref)**
- **커버링 인덱스를 통한 테이블 접근 제거 (Using index)**

<br>

**\<Explain Analyze\>**  
```sql
Covering index lookup on b using idx_board_deleted_at_created_date_desc (deleted_at=NULL) 
(actual time=0.0453..271 rows=899962 loops=1)
```

- 소요 시간: **기존 1594ms → 271ms**

<br>

### [부하 테스트 재검증]

- 멀티 컬럼 인덱스 적용 후 부하 테스트 재측정

<br>

#### k6 대시보드 Latency 평균 수치
![k6 대시보드 Latency 평균 수치](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/k6%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%ED%9B%84%20Latency.png)
- Latency: **평균 12s → 평균 720ms   (약 16배 개선)**

<br>

#### k6 대시보드 RPS, Request Failed 수치
![k6 대시보드 RPS, Request Failed 수치](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/k6%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%ED%9B%84%20RPS%2C%20Request%20Failed.png)
- RPS: **1.84/s → 25.78/s   (약 14배 개선)**
- Request Failed: **17.5% → 0%**

<br>

#### AWS CloudWatch 대시보드
![AWS CloudWatch EC2 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/EC2%20-%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%ED%9B%84%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0.png)
- EC2 서버 2대
    - CPU 사용률 **9.77% 이하**
    - Memory 사용률 **60.2% 이하**

<br>

![AWS CloudWatch RDS 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/RDS%20-%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%ED%9B%84%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0.png)
- RDS
    - CPU 사용률 **99.6% 이하**
    - 사용 가능한 메모리양 **104.1M 이상**


<br>

### [추가 개선 포인트]
- OFFSET 기반 페이징에서 페이지 증가 시 성능 저하 발생

|                               | 응답 소요 시간 |                                                                                                                                                                  측정 시간                                                                                                                                                                  |
|:-----------------------------:|:--------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|   OFFSET 0 (`LIMIT 0, 10`)    | 약 0.007초 | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20%EB%A9%80%ED%8B%B0%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |
| OFFSET 5000(`LIMIT 5000, 10`) |  약 2.7초  |                                       ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20OFFSET%205000%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png)                                        |

<br>

**Explain Analyze를 활용하여 `LIMIT 5000, 10` 쿼리 분석**  

```sql
Index lookup on b using idx_board_deleted_at_created_date_desc 
(deleted_at=NULL), with index condition: (b.deleted_at is null) 
(actual time=0.766..957 rows=5010 loops=1)
```
- 인덱스가 적용되어 있어도 5010건(`rows=5010`)의 데이터 스캔
- 소요 시간: **957ms → 약 1초**

<br>

```sql
Single-row index lookup on m using PRIMARY (member_id=b.member_id) 
(actual time=0.345..0.345 rows=1 loops=5010)
```
- JOIN을 위해 member 테이블 5010번 탐색(`loops=5010`)
- 탐색 소요 시간: 0.345ms * 5010 = 1728ms → **약 1.7초**

<br>

```sql
Nested loop inner join (actual time=0.776..2686 rows=5010 loops=1)
```

- 5010건의 데이터 스캔으로 인한 JOIN 까지 소요된 시간: **2686ms → 약 2.7초** (1초 + 1.7초)

</details>

<br>

### [부하 테스트 기반 게시판 성능 개선 최종 수치 변화]
#### RPS, Latency, Request Failed
|                | 개선 전 | 개선 후 |
|:--------------:|:---:|:---:|
|      RPS       | 1.84/s | 25.78/s |
|    Latency     | 평균 12s | 평균 720ms |
| Request Failed | 17.5% | 0% |

<br>

#### DB 쿼리 - 인덱스 적용 전후
|  | 개선 전 |   개선 후   |
|:---:|:---:|:--------:|
| 메인 쿼리 | 약 2.8초 | 약 0.007초 |
| COUNT 쿼리 | 약 1.7초 |  약 0.4초  |

<br>

---

<br>

(자세히 보기↓↓)

<details>
<summary><h2>2. 트레이드 오프 기반 게시판 정렬 기능 설계</h2></summary>

### [1차 구현 - Querydsl 기반 동적 쿼리  + 상관 서브쿼리]
#### 구현 방식
- 좋아요 수 SELECT절의 상관 서브쿼리로 집계
- 좋아요 수 기준 정렬 역시 동일한 상관 서브쿼리를 사용

<br>

**게시글 목록 조회 Repository Code**  
![게시글 목록 조회 Repository Code](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/repository%20code/%EC%83%81%EA%B4%80%20%EC%84%9C%EB%B8%8C%EC%BF%BC%EB%A6%AC%20Repository%20Code%201.png)

<br>

**좋아요 수 조회 상관 서브쿼리 Repository Code**  
![좋아요 수 조회 상관 서브쿼리 Repository Code](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/repository%20code/%EC%83%81%EA%B4%80%20%EC%84%9C%EB%B8%8C%EC%BF%BC%EB%A6%AC%20Repository%20Code%202.png)

<br>

**좋아요 수 내림차순 정렬 Repository Code**  
![좋아요 수 내림차순 정렬 Repository Code](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/repository%20code/%EC%83%81%EA%B4%80%20%EC%84%9C%EB%B8%8C%EC%BF%BC%EB%A6%AC%20Repository%20Code%203.png)

<br>

### [발생한 문제 인식]
**상관 서브쿼리를 사용한 방식의 구조적 한계**  
- 게시글 row 마다 좋아요 집계 연산 수행
- 좋아요 수 정렬 시 동일 집계 연산 반복

<br>

**Explain Analyze를 통한 실제 쿼리 수행 확인**  
```sql
-> Select #3 (subquery in projection; dependent)
    -> Aggregate: count(l2.like_id)  
        -> Covering index lookup on l2 using uq_board_member (board_id=b.board_id) 
-> Select #2 (subquery in projection; dependent)
    -> Aggregate: count(l.like_id) 
        -> Covering index lookup on l using uq_board_member (board_id=b.board_id)
```
- **Dependent Subquery 2번 실행**

<br>

| 응답 소요 시간 | 측정 시간 |
|:--------:|:---:|
|  약 6.8초  | ![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/correlated%20subquery/%EC%83%81%EA%B4%80%20%EC%84%9C%EB%B8%8C%EC%BF%BC%EB%A6%AC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%A0%84%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png) |

<br>

### [대안 성능 분석]

#### 성능 분석 데이터 환경
- 게시판/ 회원 테이블 데이터 각각 100만
- 게시판 삭제된 수 10만 (Sofe Delete → deleted_at 컬럼 NULL 유무 판단)
- 좋아요 테이블 데이터 20만

<br>

**상관 서브쿼리**  
```sql
SELECT 
	b.board_id,
	b.title,
	b.view_count,
	b.created_date,
	m.name,
	(SELECT COUNT(l.like_id) FROM likes l WHERE l.board_id = b.board_id) 
FROM 
	board b 
JOIN 
	member m ON m.member_id = b.member_id 
WHERE
	b.deleted_at IS NULL
ORDER BY
	(SELECT COUNT(l2.like_id) FROM likes l2 WHERE l2.board_id = b.board_id) DESC,
	b.board_id
LIMIT 0,10;
```

<br>

**인라인 뷰 쿼리**  
```sql
SELECT
	b.board_id,
	b.title,
	b.view_count,
	b.created_date,
	m.name,
	lc.likeCount 
FROM
	board b
JOIN 
	member m ON m.member_id = b.member_id 
LEFT JOIN 
	(SELECT 
		l.board_id,
		COUNT(l.like_id)
	 FROM 
	 	likes l 
 	 GROUP BY
 	 	l.board_id
 	) lc(boardId,likeCount) ON lc.boardId = b.board_id 
WHERE 
	b.deleted_at IS NULL 
ORDER BY 
	lc.likeCount DESC,
	b.board_id 
LIMIT 0,10;
```

<br>

|  방법   | 응답 소요 시간 |                                                                                                                                                    측정 시간                                                                                                                                                     | deleted_at 컬럼 인덱스 적용 후 |                                                                                                                                                    측정 시간                                                                                                                                                     |
|:-----:|:--------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:----------------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| 인라인 뷰 | 약 1분 15초 |              ![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/inline%20view/%EC%9D%B8%EB%9D%BC%EC%9D%B8%20%EB%B7%B0%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%A0%84%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png)              |        약 1분 22초        |              ![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/inline%20view/%EC%9D%B8%EB%9D%BC%EC%9D%B8%20%EB%B7%B0%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%ED%9B%84%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png)              |
| 상관 서브쿼리 |  약 6.8초   | ![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/correlated%20subquery/%EC%83%81%EA%B4%80%20%EC%84%9C%EB%B8%8C%EC%BF%BC%EB%A6%AC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%A0%84%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png) |         약 11초          | ![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/correlated%20subquery/%EC%83%81%EA%B4%80%20%EC%84%9C%EB%B8%8C%EC%BF%BC%EB%A6%AC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%ED%9B%84%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png) |

<br>

- deleted_at 컬럼 인덱스
```sql
CREATE INDEX idx_board_deleted_at ON board (deleted_at);
```

<br>

**Explain Analyze를 활용하여 ‘인라인 뷰’와 ‘상관 서브쿼리’를 비교 분석**  
**<공통점>**  
1. 응답 소요 시간이 긴 이유 → **풀테이블 스캔**
    - 인라인 뷰
    ```sql
    Table scan on b (actual time=2.96..1449 rows=1e+6 loops=1) 
    ```
   
   <br>

   - 상관 서브쿼리
   ```sql
   Table scan on b (actual time=1.61..1476 rows=1e+6 loops=1)
   ```

    두 방식 다 **풀테이블 스캔**
    
    소요 시간: **약 1.5초**

<br>

2. 인덱스를 적용해도 더 느린 이유 → **디스크 랜덤 I/O 발생**
    - 인라인 뷰
   ```sql
   Index lookup on b using idx_board_deleted_at 
   (deleted_at=NULL), with index condition: (b.deleted_at is null) 
   (actual time=1.59..5874 rows=899962 loops=1)
   ```
   
   <br>
   
   - 상관 서브쿼리
   ```sql
   Index lookup on b using idx_board_deleted_at 
   (deleted_at=NULL), with index condition: (b.deleted_at is null) 
   (actual time=2.95..5711 rows=899962 loops=1)
   ```

    두 방식 다 `with index condition`  인덱스 기반 필터링 진행해도(`rows=899962`) 약 90만 건 데이터 스캔
    
    약 90만 건의 디스크 랜덤 I/O 발생: **약 5.8초**
    
    풀테이블 스캔 **(약 1.5초)** 보다 **4.3초 오래 걸림**

<br>

**<차이점>**  
1. 상관 서브쿼리가 더 빠른 이유
    ```sql
   Sort: (select #3) DESC, b.board_id (actual time=6727..6727 rows=10 loops=1)
    ```
   - 정렬 작업 먼저 진행 후 10개 데이터(`rows=10`)로 데이터 양 감소
   
   <br>
   <br>
   
   ```sql
   Single-row index lookup on m using PRIMARY (member_id=b.member_id)
   (actual time=1.92..1.92 rows=1 loops=10)
   ```
   - member 테이블 탐색 10번(`loops=10`)
   
   <br>
   <br>
   
   ```sql
   Nested loop inner join (actual time=6747..6748 rows=10 loops=1)
   ```
   - JOIN까지 진행하는데 소요된 시간: **약 6.7초**

<br>

2. 인라인 뷰가 더 느린 이유
    ```sql
    Filter: (b.deleted_at is null) (actual time=2.97..1596 rows=899962 loops=1)
    ```
   - WHERE절 조건으로 필터링 후 남은 데이터 수 약 90만 건(`rows=899962`)

   <br>
   <br>

   ```sql
   Single-row index lookup on m using PRIMARY (member_id=b.member_id) 
   (actual time=0.0793..0.0794 rows=1 loops=899962)
   ```
   - member 테이블 탐색 약 90만(`loops=899962`)

   <br>
   <br>

   ```sql
   Nested loop inner join (actual time=3.58..73241 rows=899962 loops=1)
   ```
   - JOIN까지 진행하는데 소요된 시간: **약 73초(1분 13초)**

<br>
    
&nbsp; &nbsp; &nbsp; &nbsp; JOIN 연산 단계에서 **데이터 양(`rows=10 / rows=899962`)** 에 의한 성능 차이 발생

<br>

### [최종 선택]
- ‘완전한 좋아요 수 정렬’ 대신 ‘최신 게시글 100건을 기준으로 좋아요 수를 집계’하여 조회하도록 변경
- Querydsl에서 인라인 뷰 사용 제한으로 인해 쿼리를 ‘최신 게시글 ID 조회’ 와 ‘좋아요 수 집계’ 2단계로 분리

<br>

**최신 게시판 ID 100건 조회 쿼리**  
```sql
SELECT 
	b.board_id 
FROM 
	board b 
JOIN 
	member m ON m.member_id = b.member_id 
WHERE 
	b.deleted_at IS NULL 
ORDER BY 
	b.created_date DESC,
	b.board_id 
LIMIT 100;
```

- 멀티 컬럼 인덱스 적용
```sql
CREATE index idx_board_deleted_at_created_date_desc ON board (deleted_at, created_date DESC);
```

| 응답 소요 시간 | 측정 시간 |
|:--------:|:---:|
| 약 0.007초 | ![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/after/%EC%B5%9C%EC%8B%A0%20100%EA%B1%B4%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png) |

<br>

**최신 게시글 ID 100건을 기준으로 좋아요 수를 집계 쿼리**  
```sql
SELECT
	b.board_id,
	b.title,
	b.view_count,
	b.created_date,
	m.name,
	lc.likeCount 
FROM
	board b
JOIN 
	member m ON m.member_id = b.member_id 
LEFT JOIN 
	(SELECT 
		l.board_id,
		COUNT(l.like_id) 
	 FROM 
	 	likes l 
 	 GROUP BY 
 	 	l.board_id
 	) lc(boardId,likeCount) ON lc.boardId = b.board_id 
WHERE 
	b.deleted_at IS NULL 
AND 
	b.board_id IN (게시글 ID 100개) 
ORDER BY
	lc.likeCount DESC,
	b.created_date DESC,
	b.board_id 
LIMIT 0, 10;
```

| 응답 소요 시간 | 측정 시간 |
|:--------:|:---:|
| 약 0.067초 | ![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/after/%EC%B5%9C%EC%8B%A0%20100%EA%B1%B4%20%EC%A2%8B%EC%95%84%EC%9A%94%20%EC%88%98%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png) |
</details>

<br>

### [트레이드 오프 기반 게시판 정렬 기능 최종 성능 변화]
#### 응답 소요 시간

| 상관 서브쿼리 | 게시글 최신 100건 기준 좋아요 수 정렬 쿼리 |
|:---:|:--------------------------:|
| 약 6.8초 |          약 0.074초          |

<br>

---

<br>

(자세히 보기↓↓)

<details>
<summary><h2>3. AWS VPC를 활용한 보안, 고가용성 인프라 구축</h2></summary>

### [VPC 재구성 후 인프라 구조]  
![VPC 재구성 후 인프라 구조](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/aws-infra/image/AWS%20%EC%9D%B8%ED%94%84%EB%9D%BC%20%EA%B5%AC%EC%A1%B0.png)

<br>

### [요청 흐름]
#### 사용자 요청 흐름
![사용자 요청 흐름](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/aws-infra/image/AWS%20%EC%9D%B8%ED%94%84%EB%9D%BC%20%EC%82%AC%EC%9A%A9%EC%9E%90%20%EC%9A%94%EC%B2%AD%20%ED%9D%90%EB%A6%84.png)  

- 사용자 ALB로 요청
- ALB를 통해 사용자 요청 트래픽 분산 처리
- 각 서버 Primary DB에 요청(Read / Write)

<br>

#### 관리자 요청 흐름
![관리자 요청 흐름](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/aws-infra/image/AWS%20%EC%9D%B8%ED%94%84%EB%9D%BC%20%EA%B4%80%EB%A6%AC%EC%9E%90%20%EC%9A%94%EC%B2%AD%20%ED%9D%90%EB%A6%84.png)  

- 관리자 Bastion Host를 통해 VPC 내부 자원 접근(서버, RDS)

<br>

#### 서버 요청 흐름
![서버 요청 흐름](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/aws-infra/image/AWS%20%EC%9D%B8%ED%94%84%EB%9D%BC%20EC2%20%EC%84%9C%EB%B2%84%20%EC%9A%94%EC%B2%AD%20%ED%9D%90%EB%A6%84.png)

- 서버는 NAT 게이트웨이를 통해 외부 인터넷 접근 가능(패키지 설치, 외부 API 요청 등)
- 외부 인터넷에서 직접 서버 접근 차단
</details>