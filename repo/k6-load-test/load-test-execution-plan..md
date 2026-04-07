# 부하 테스트 기반 게시판 성능 개선 (k6 + 인덱스 최적화)

## 1. 부하 테스트 기반 문제 발견

- 부하테스트 사진 첨부

## 2. 병목 원인 분석

- 커넥션 풀 확장 후 부하테스트 사진 첨부
- 


## 실행된 쿼리
### 게시글 최신순으로 가져오는 쿼리

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
  b.created_date DESC LIMIT 0,10;
```

### 페이징 처리를 위한 COUNT 쿼리

```sql
SELECT 
  count(b.board_id) 
FROM 
  board b 
WHERE 
  b.deleted_at IS NULL;
```

## 인덱스 적용 전 후 비교
**[적용 인덱스]**

- 단일 인덱스(created_date DESC)
- 복합 인덱스(deleted_at, created_date DESC)

**[k6 대쉬보드 + EC2,RDS CPU Memory 사용률]**

- 캡처 사진 넣고 비교 글 작성

**[소요 시간]**
1. 원본 쿼리

    |           인덱스 미적용           |  단일 컬럼 인덱스  |       멀티 컬럼 인덱스       |
    |:---------------------------:|:-----------:|:---------------------:|
    | 약 2초 | 약 0.004초 | 약 0.003초 | 

   - 단일 컬럼 인덱스 적용 후: 약 2초 → 0.004초
   - 멀티 컬럼 인덱스 적용 후: 약 2초 → 0.003초
   - 단일 컬럼와 멀티 컬럼 인덱스의 차이 미비


2. COUNT 쿼리

    | 인덱스 미적용 | 단일 컬럼 인덱스 | 멀티 컬럼 인덱스 |
    |:-------:|:---------:|:---------:|
    |  약 1초   |   약 1초    |  약 0.4초   |

    - 단일 컬럼 인덱스 적용 후: 시간 차이 없음
    - 멀티 컬럼 인덱스 적용 후: 약 1초 → 약 0.4초

**[Explain]**
1. 원본 쿼리 비교

    |          |           인덱스 미적용           |  단일 컬럼 인덱스  |       멀티 컬럼 인덱스       |
    |----------|:---------------------------:|:-----------:|:---------------------:|
    | type     |             ALL             |    index    |          ref          |
    | Extra    | Using where; Using filesort | Using where | Using index condition | 

   - 비교 글 작성
   - 단일 컬럼 인덱스 적용 후 풀테이블 스캔 -> 인덱스 스캔으로  변경

2. COUNT 쿼리 비교

   |          |           인덱스 미적용           |  단일 컬럼 인덱스  |        멀티 컬럼 인덱스         |
   |----------|:---------------------------:|:-----------:|:------------------------:|
   | type     |             ALL             |     ALL     |           ref            |
   | Extra    | Using where | Using where | Using where; Using index | 

    - 비교 글 작성

    
**[Explain Analyze]**  
1. 원본 쿼리 비교

    1. 인덱스 적용 전
    - `Table scan on b (actual time=15.2..1183 rows=1e+6 loops=1)`
      - 풀 테이블 스캔으로 100만건 데이터 스캔하는데 **약 1.2초** 소요
    - `Sort: b.created_date DESC (actual time=2002..2002 rows=10 loops=1)`
      - 정렬 작업 **약 0.8초** 소요
    
    2. 단일 컬럼 인덱스 적용
    - `Index scan on b using idx_board_created_date_desc (actual time=0.1..0.2 rows=13 loops=1)`
      - 인덱스 스캔으로 인덱스 테이블 순차 접근, row 13, 소요 시간 0.2 밀리초
    - 정렬 작업 제거

    3. 멀티 컬럼 인덱스 적용
    - `Index lookup on b using idx_board_deleted_at_created_date_desc (deleted_at=NULL), with index condition: (b.deleted_at is null)  (actual time=0.311..0.472 rows=10 loops=1)`
      - ㅇㅇㅇ




### 부하테스트 환경

**[인덱스 적용 전]**

**소요 시간:** 약 2초 

**[Explain]**  

| table | type | key | Extra                        |  
|-------|-------|------|------------------------------|
| board | ALL | [NULL] | Using where; Using filesort  | 
| member | eq_ref | PRIMARY | [NULL] | 




**[Explain Analyze]**  
주요 병목 구간  
1.  `Table scan on b (actual time=15.2..1183 rows=1e+6 loops=1)`  
    - 풀 테이블 스캔으로 100만건 데이터 스캔하는데 약 1.2초 소요

2. `Sort: b.created_date DESC (actual time=2002..2002 rows=10 loops=1)`  
    - 정렬 약 0.8초 소요

- 풀 테이블 스캔 + 정렬이 주요 병목

**[인덱스 적용 후]**  
- 단일 인덱스(created_date DESC) 적용
    - **소요 시간:** 약 0.004초  


**[Explain]**  

| table | type   | key                         | Extra                        | 
|-------|--------|-----------------------------|------------------------------|  
| board | index  | idx_board_created_date_desc | Using where |  
| member | eq_ref | PRIMARY                     | [NULL] |  


**[Explain Analyze]**  
주요 변화
1. `Index scan on b using idx_board_created_date_desc  (actual time=0.1..0.2 rows=13 loops=1)`
    - 인덱스 스캔으로 인덱스 테이블 순차 접근, row 13, 소요 시간 0.2 밀리초
2. 정렬 작업 사라짐

