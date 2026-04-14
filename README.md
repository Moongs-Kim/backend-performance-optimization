
## 목차
1. [부하 테스트 기반 게시판 성능 개선 (k6 + 인덱스 최적화)](#1-부하-테스트-기반-게시판-성능-개선-k6--인덱스-최적화)
2. [트레이드 오프 기반 게시판 정렬 기능 설계](#2-트레이드-오프-기반-게시판-정렬-기능-설계)
3. [AWS VPC를 활용한 보안, 고가용성 인프라 구축](#3-aws-vpc를-활용한-보안-고가용성-인프라-구축)

<br>

## 1. 부하 테스트 기반 게시판 성능 개선 (k6 + 인덱스 최적화)

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
#### 1. **단일 컬럼 인덱스 적용**
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
- 소요 시간: **기존 481ms → 0.098ms**

<br>

#### COUNT 쿼리 Explain
- 실행 계획 **변화 없음**

<br>

#### 2. **멀티 컬럼 인덱스 적용**  
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
Index lookup on b using idx_board_deleted_at_created_date_desc 
(deleted_at=NULL), with index condition: (b.deleted_at is null) 
(actual time=0.0612..0.0892 rows=10 loops=1)
```
- 인덱스 기반 필터링으로 LIMIT 10 에 맞는 데이터만 조회 `rows=10`

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

**Explain Analyze**  
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
![AWS CloudWatch 대시보드](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/EC2%2C%20RDS%20-%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%ED%9B%84%20CPU%2C%20Memory%20%EC%82%AC%EC%9A%A9%EB%A5%A0.png)
- EC2 서버 2대
    - CPU 사용률 **9.77% 이하**
    - Memory 사용률 **60.2% 이하**
- RDS
    - CPU 사용률 **99.6% 이하**
    - 사용 가능한 메모리양 **104.1M 이상**


<br>

### [추가 개선 포인트]
- OFFSET 기반 페이징에서 페이지 증가 시 성능 저하 발생

|     OFFSET 0 (`LIMIT 0, 10`)      |  약 0.007초  | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20%EB%A9%80%ED%8B%B0%20%EC%BB%AC%EB%9F%BC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |
|:---------------------------------:|:----------:|:----------:|
| **OFFSET 5000(`LIMIT 5000, 10`)** | **약 2.7초** | ![소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/k6-load-test/image/index/%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EC%B5%9C%EC%8B%A0%EC%88%9C%20%EC%A1%B0%ED%9A%8C%20OFFSET%205000%20%EC%86%8C%EC%9A%94%EC%8B%9C%EA%B0%84.png) |

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

<br>

---

<br>

## 2. 트레이드 오프 기반 게시판 정렬 기능 설계

### [성능 분석 데이터 환경]
- 게시판/ 회원 테이블 데이터 각각 100만
- 게시판 삭제된 수 10만 (Sofe Delete → deleted_at 컬럼 NULL 유무 판단)
- 좋아요 테이블 데이터 20만

<br>

### [상황]
- 기존 게시판 목록 조회는 단순 페이징 기반 조회 기능만 제공

  게시판 목록 조회 Repository Code  
  ![초기 게시판 목록 조회 Repository Code](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/repository%20code/%EC%B4%88%EA%B8%B0%20%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EB%AA%A9%EB%A1%9D%20%EC%A1%B0%ED%9A%8C%20Repository%20Code.png)
    - Fetch Join 사용

<br>

- 서비스 확장을 위해 다음 기능을 고려
    - 제목 / 작성자 조건 검색
    - 최신순 / 조회순 / 좋아요순 정렬

<br>

- 게시판과 좋아요 테이블의 관계
    - 게시판 (1) : 좋아요 (N)
    - 게시판 테이블에는 좋아요 수 집계 컬럼이 존재하지 않는 정규화 유지 상태

<br>

### [1차 구현 - Querydsl 기반 동적 쿼리  + 상관 서브쿼리]
#### 구현 방식  
- Querydsl을 활용하여 검색 조건 및 정렬 조건을 동적으로 처리
- Fetch Join 대신 DTO Projection을 사용하여 필요한 컬럼만 조회
- 좋아요 수는 SELECT절의 상관 서브쿼리로 집계
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

#### 선택 이유  
- Fetch Join은 게시판 (1) : 좋아요 (N) 관계에서 row 증가로 인해 DB 레벨 페이징이 불가능
- 좋아요 수는 단순 조인이 아닌 집계값이므로 Fetch Join으로 해결 불가
- Querydsl을 사용하면 동적 쿼리 관리가 용이

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
- **실제로 Dependent Subquery가 2번 실행**

<br>

**응답 소요 시간 확인: 약 6.8초**  
![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/correlated%20subquery/%EC%83%81%EA%B4%80%20%EC%84%9C%EB%B8%8C%EC%BF%BC%EB%A6%AC%20%EC%9D%B8%EB%8D%B1%EC%8A%A4%20%EC%A0%81%EC%9A%A9%20%EC%A0%84%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png)

<br>

- **따라서 데이터가 증가할 경우 게시판 목록 API의 응답 시간이 급격히 증가할 수 있다고 판단**

<br>

### [대안 검토]
- 좋아요 수 기준 정렬을 유지하기 위해 다음 선택지를 검토

|  방법   | 장점           | 단점                                            |
|:-----:|:-------------|:----------------------------------------------|
| 인라인 뷰 | 단일 쿼리 처리 가능  | • Querydsl 사용 불가 <br> • 동적 쿼리 작성 어려움          |
| 역정규화 | 조회 성능 우수     | • 좋아요 수 증감 시 동시성 문제 발생 가능 <br>• 데이터 정합성 관리 필요 |
| 상관 서브쿼리 | 구현 단순        | 집계 비용 증가                                      |

<br>

### [대안 성능 분석]
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
    
&nbsp; &nbsp; &nbsp; &nbsp; JOIN 연산 단계에서 **데이터 양(`rows=899962 / rows=10`)** 에 의한 성능 차이 발생  

<br>

- 이를 통해 쿼리 연산 초기 단계에서 **데이터 범위를 줄이지 못하는 구조**가 전체 성능 병목의 핵심 원인임을 확인
- 역정규화를 통한 해결 방안도 검토하였으나 좋아요 수 변경 시 데이터 정합성과 동시성 제어 복잡도가 증가하는 트레이드 오프가 있다고 판단
- 따라서 현재 구조에서는 대용량 데이터에서 집계 기반 정렬 쿼리의 구조적 한계를 인지하고
  **조회 범위를 제한하는 방법**을 생각

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

- 응답 소요 시간: **약 0.007초**  
![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/after/%EC%B5%9C%EC%8B%A0%20100%EA%B1%B4%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png)

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

- 응답 소요 시간: **약 0.067초**  
![응답 소요 시간](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/trade-off-base/image/after/%EC%B5%9C%EC%8B%A0%20100%EA%B1%B4%20%EC%A2%8B%EC%95%84%EC%9A%94%20%EC%88%98%20%EC%86%8C%EC%9A%94%20%EC%8B%9C%EA%B0%84.png)

- 조회 범위를 축소하는 구조로 변경함으로써 처리해야 할 데이터 양 감소
- 기존에는 활용하기 어려웠던 인덱스를 효과적으로 적용할 수 있는 구조로 개선하여 쿼리 성능을 향상

<br>

- 그 결과 **상관 서브쿼리를 제거**하여 데이터 건수에 따라 증가하던 쿼리 비용 증가 문제를 해결
- **제한된 범위 내에서 좋아요 기반 정렬 기능을 유지**

<br>

#### [향후 개선 가능성]
**완전한 좋아요 수 정렬 기능을 위해**  
- 좋아요 수를 캐싱하여 조회 시 반복적인 집계 연산을 줄이는 방안을 고려
- 게시판 테이블에 좋아요 수 컬럼을 추가하는 역정규화 전략을 통해 조회 성능 향상시키는 방안을 고려

<br>

---

<br>

## 3. AWS VPC를 활용한 보안, 고가용성 인프라 구축
### [도입 배경]
- AWS의 기본 VPC 환경에서 EC2, RDS가 퍼블릭하게 노출된 구조
- 단일 서버 + 단일 AZ 구조로 장애 발생 시 서비스 전체 중단 위험
- 외부 사용자와 관리자 접근 경로가 분리되지 않아 보안 통제가 어려운 구조

<br>

### [설계 및 개선 내용]
#### VPC 재구성 후 인프라 구조  
![VPC 재구성 후 인프라 구조](https://github.com/Moongs-Kim/backend-performance-optimization/blob/main/repo/aws-infra/image/AWS%20%EC%9D%B8%ED%94%84%EB%9D%BC%20%EA%B5%AC%EC%A1%B0.png)

#### 보안  
- VPC를 재구성하여 **Public / Private Subnet 구조로 분리**
    - 서버(EC2), RDS를 Private Subnet에 배치하여 외부 직접 접근을 차단
- Public Subnet에 **Bastion Host**를 구성하여 관리자 접근 경로를 단일화
- Private Subnet 인스턴스의 외부 통신을 위해 **NAT Gateway** 구성
    - 내부 자원을 외부에 노출하지 않으면서도 외부 통신이 가능한 구조로 개선

<br>

- **보안 그룹 최소 권한 원칙** 적용
    - Bastion Host → SSH 허용 (관리자 IP 제한)
    - ALB → HTTP(80) / HTTPS(443) 허용
    - 서버 → ALB, Bastion Host만 접근 허용
    - RDS → 서버, Bastion Host만 접근 허용

<br>

#### 가용성  
- **멀티 AZ + 멀티 서버 구조로 전환**
    - 서버를 가용 영역 ap-northeast-2a / ap-northeast-2b 에 분산 배치
    - ALB를 통해 트래픽 분산 처리
    - RDS Multi-AZ 구성으로 장애 시 자동으로 Standby DB로 전환

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

<br>

### [성과]
- Public Subnet 구조를 Private Subnet + NAT Gateway 구조로 변경하여 **외부에서의 직접 접근을 차단하고 Outbound 통신만 허용하도록 개선**
- 내부 접근 경로를 Bastion Host로 단일화 하여 **보안 관리 범위를 축소**
- 단일 장애 지점을 제거하여 **고가용성 구조를 확보**
    - 실제 서버 1대를 중단한 상황에서도 서비스가 정상 동작함을 확인
- ALB 기반 구조로 전환하여 **수평 확장이 가능한 구조로 개선**
    - EC2 서버가 병목 지점일 경우 인스턴스를 추가하여 성능을 확장할 수 있는 구조로 개선