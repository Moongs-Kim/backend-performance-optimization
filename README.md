
## 1. 부하 테스트 기반 게시판 성능 개선 (k6 + 인덱스 최적화)

### [부하 테스트 환경]

#### 인프라 구조

![부하 테스트 인프라 구조](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/%EB%B6%80%ED%95%98%20%ED%85%8C%EC%8A%A4%ED%8A%B8%20%EC%9D%B8%ED%94%84%EB%9D%BC%20%EA%B5%AC%EC%A1%B0.png)

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
![AWS CloudWatch 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/EC2%2C%20RDS%20-%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0(%EA%B0%9C%EC%84%A0%20%EC%A0%84).png)
- EC2 서버 2대
    - CPU 사용률 **3.76% 이하**
    - Memory 사용률 **59.2% 이하**
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

- **이를 통해 요청 대기(Queue)로 인한 병목 발생으로 판단**

<br>

### [병목 원인 분석]
1. **HikariCP 커넥션 풀 확장 후 다시 테스트 진행**
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
![AWS CloudWatch 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/connection%20pool/EC2%2C%20RDS%20-%20%EC%BB%A4%EB%84%A5%EC%85%98%20%ED%92%80%20%ED%99%95%EC%9E%A5%20%ED%9B%84%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0.png)
- EC2 서버 2대
    - CPU 사용률 **3.58% 이하**
    - Memory 사용률 **57.9% 이하**
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

- DB 커넥션 풀을 확장해도 큰 변화 없음
- **DB 커넥션 부족이 근본적인 원인이 아님을 파악**

<br>

2. **DB 쿼리 성능을 확인하기 위해 응답 소요 시간 확인**

|    메인 쿼리     |   약 2.8초   |                ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20%EA%B0%9C%EC%84%A0%20%EC%A0%84%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png)                 |
|:------------:|:----------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| **COUNT 쿼리** | **약 1.7초** | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/before/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%B9%B4%EC%9A%B4%ED%8A%B8%20%EC%BF%BC%EB%A6%AC%20%EA%B0%9C%EC%84%A0%20%EC%A0%84%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png)  |

<br>

#### Explain Analyze를 통한 병목 확인
**<메인 쿼리>**
```sql
Table scan on b (actual time=0.767..481 rows=1e+6 loops=1)
``` 
약 100만 건(`rows=1e+6`) 풀테이블 스캔
- 정렬 전 WHERE절 조건 필터링 소요 시간: **0.558ms**
- 필터링 후 남은 rows: **약 90만**
- 정렬 소요 시간: 2861ms - 0.558ms = 약 2860ms → **약 2.8초**

<br>

**<COUNT 쿼리>**  
```sql
Table scan on b (actual time=2.16..1594 rows=1e+6 loops=1)
``` 
약 100만 건(`rows=1e+6`) 풀테이블 스캔
- 소요 시간: **약 1.6초**

<br>

- 메인 쿼리에서 **약 90만 건 정렬**이 주요 병목임을 확인
- COUNT 쿼리에서 **풀테이블 스캔**이 주요 병목임을 확인

<br>

### [성능 개선 - 인덱스 설계]
1. **단일 컬럼 인덱스 적용**
```sql
CREATE INDEX idx_board_created_date_desc ON board (created_date DESC);
```
- 메인 쿼리의 주요 병목인 **정렬 작업을 제거하기 위해** ORDER BY절에 사용하는 컬럼에 인덱스 적용

<br>

|     | 기존 응답 소요 시간  | 단일 컬럼 인덱스 적용 후 응답 소요 시간  | 소요 시간 이미지  |
|:---:|:------------:|:------------------------:|:----------:|
| 메인 쿼리 | 약 2.8초 | 약 0.008초 | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20%EB%8B%A8%EC%9D%BC%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |
| COUNT 쿼리 | 약 1.7초 | 약 1.7초 | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%B9%B4%EC%9A%B4%ED%8A%B8%20%EC%BF%BC%EB%A6%AC%20%EB%8B%A8%EC%9D%BC%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |

- 메인쿼리: **약 2.8초 → 약 0.008초 (약 350배 개선)**
- COUNT 쿼리: **차이 미미**

<br>

#### 메인 쿼리  

**Explain**  

|      | 인덱스 적용 전 |   인덱스 적용 후   |
|:----:|:--------:|:------------:|
| type |   ALL    |    index     |
|Extra | Using where; Using filesort | Using where  |

- **풀테이블 스캔 (ALL)** → **인덱스 스캔 (index)**
- **정렬 작업 제거 (Using filesort)**

<br>

**Explain Analyze**  
```sql
Index scan on b using idx_board_created_date_desc (actual time=0.055..0.098 rows=13 loops=1)
```
- 약 100만 건(`rows=1e+6`) 풀테이블 스캔 → `rows=13`으로 데이터 스캔 양 감소
- 소요 시간: **기존 0.481ms → 0.098ms (약 5배 개선)**

<br>

#### COUNT 쿼리 Explain
- 실행 계획 **변화 없음**

<br>

2. **멀티 컬럼 인덱스 적용**  
```sql
CREATE INDEX idx_board_deleted_at_created_date_desc ON board (deleted_at, created_date DESC);
```
- COUNT 쿼리의 주요 병목인 **풀테이블 스캔을 제거하기 위해** WHERE절에 사용하는 컬럼에 인덱스 적용
- 메인 쿼리와 COUNT 쿼리의 WHERE절이 동일
    - 메인 쿼리의 WHERE절 필터링과 ORDER BY절 정렬을 고려하여 멀티 컬럼 인덱스 적용

<br>

|     | 단일 컬럼 인덱스 적용 후 응답 소요 시간  | 멀티 컬럼 인덱스 적용 후 응답 소요 시간 | 소요 시간 이미지  |
|:---:|:------------:|:-----------------------:|:----------:|
| 메인 쿼리 | 약 0.008초 | 약 0.007초 | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20%EB%A9%80%ED%8B%B0%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |
|COUNT 쿼리 | 약 1.7초 | 약 0.4초 | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%B9%B4%EC%9A%B4%ED%8A%B8%20%EC%BF%BC%EB%A6%AC%20%EB%A9%80%ED%8B%B0%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |


- 메인쿼리: **차이 미미**
- COUNT 쿼리: **약 1.7초 → 약 0.4초 (약 4배 개선)**

<br>

#### 메인 쿼리 
**Explain**  

|      | 단일 컬럼 인덱스 적용 | 멀티 컬럼 인덱스 적용 |
|:----:|:---:|:---:|
| type | index | ref |
| Extra | Using where | Using index condition |

- **인덱스 스캔 (index) → 인덱스 참조 스캔 (ref)**
- **인덱스 기반 필터링으로 변경 (Using index condition)**

<br>

**Explain Analyze**  
```sql
Index lookup on b using idx_board_deleted_at_created_date_desc (deleted_at=NULL), with index condition: (b.deleted_at is null) 
(actual time=0.0612..0.0892 rows=10 loops=1)
```
- 인덱스 기반 필터링으로 LIMIT 10에 맞는 데이터만 조회 `rows=10`

<br>

#### COUNT 쿼리
**Explain**  

|      | 단일 컬럼 인덱스 적용 | 멀티 컬럼 인덱스 적용 |
|:----:|:---:|:---:|
| type | ALL | ref |
| Extra | Using where | Using where; Using index |

- **풀테이블 스캔 (ALL) → 인덱스 참조 스캔 (ref)**
- **커버링 인덱스를 통한 테이블 접근 제거 (Using index)**

<br>

**<Explain Analyze>**  
```sql
Covering index lookup on b using idx_board_deleted_at_created_date_desc (deleted_at=NULL) 
(actual time=0.0453..271 rows=899962 loops=1)
```

- 기존 풀테이블 스캔: **1594ms**
- 인덱스 적용 후: **0.271ms (약 5800배 개선)**

<br>

- **멀티 컬럼 인덱스로 ‘메인 쿼리’ 및 ‘COUNT 쿼리’ 의 성능을 개선 가능**
- **인덱스 기반 필터링으로 불필요한 테이블 접근을 최소화 시킬 수 있어 멀티 컬럼 인덱스를 적용하기로 판단**

<br>

### [부하 테스트 재검증]
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
![AWS CloudWatch 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/EC2%2C%20RDS%20-%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%ED%9B%84%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0.png)
- EC2 서버 2대
    - CPU 사용률 **9.77% 이하**
    - Memory 사용률 **60.2% 이하**
- RDS
    - CPU 사용률 **99.6% 이하**
    - 사용 가능한 메모리양 **104.1M 이상**

<br>

- **이를 통해 DB 쿼리 성능 개선이 전체 시스템 성능 향상으로 이어짐을 확인했습니다.**

<br>

### [추가 개선 포인트]
- COUNT 쿼리는 데이터 특성상 인덱스만으로 한계가 있어 캐싱 또는 역정규화 방식으로 개선 가능
    - 인덱스를 통한 쿼리 성능 개선 진행 후 RDS의 CPU 사용률이 99%까지 향상
    - 약 90만 건의 데이터 스캔으로 약 0.4초가 소요되는 COUNT 쿼리를 개선한다면
      RDS 부하를 줄일 수 있을 것으로 판단

<br>

- OFFSET 기반 페이징에서 페이지 증가 시 성능 저하가 발생하는 문제를 확인

|     OFFSET 0 (`LIMIT 0, 10`)      |  약 0.007초  | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20%EB%A9%80%ED%8B%B0%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |
|:---------------------------------:|:----------:|:----------:|
| **OFFSET 5000(`LIMIT 5000, 10`)** | **약 2.7초** | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20OFFSET%205000%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |

<br>

```sql
Index lookup on b using idx_board_deleted_at_created_date_desc (deleted_at=NULL), with index condition: (b.deleted_at is null) 
(actual time=0.766..957 rows=5010 loops=1)
```
- 인덱스가 적용되어 있어도 5010건(`rows=5010`)의 데이터 스캔

<br>

```sql
Nested loop inner join (actual time=0.776..2686 rows=5010 loops=1)
```

```sql
Single-row index lookup on m using PRIMARY (member_id=b.member_id) 
(actual time=0.345..0.345 rows=1 loops=5010)
```
- JOIN을 위해 member 테이블 5010번 탐색(`loops=5010`)
    - 0.345ms * 5010 = 1728ms → **약 1.7초**
- member 테이블과 JOIN
    - 2686ms - 1728ms = 958ms → **약 1초**
- 5010건의 데이터 스캔으로 인한 JOIN 소요 시간
    - 1.7초 + 1초 = **2.7초**

<br>

- 이를 개선하기 위해 Cursor 기반 페이징 적용 가능성을 검토