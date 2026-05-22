
# ---------- 서버 검증 설계 ----------

1. 서버가 UGS JWT를 검증한다.

2. 검증된 토큰에서 sub, jti, sign_in_provider를 꺼낸다.

3. DB 트랜잭션을 시작한다.

4. consumed_ugs_jti 테이블에 jti를 INSERT 한다.
   - jti는 UGS 토큰 재사용 방지용
   - jti가 이미 있으면 중복 로그인 요청 또는 replay로 보고 실패 처리
   - unique constraint / primary key로 막는다.

5. user_identities에서
   provider = 'unity_ugs'
   provider_subject = sub
   인 유저를 조회한다.

6. 기존 identity가 있으면 해당 user_id로 로그인 처리한다.

7. 기존 identity가 없으면 신규 유저 등록을 수행한다.
   - users 테이블에 자체 user_id 생성
   - user_identities 테이블에 UGS sub와 user_id 연결
   - 필요하면 최소 기본 프로필/게임 데이터 초기 row 생성

8. sessions 테이블에 새 세션을 생성한다.
   - session_id 생성
   - refresh token 원문은 클라에만 전달 
   - DB에는 refresh token hash만 저장 (비밀번호를 db에 저장하지 않는것처럼 탈취 노출 위험 방지)
   - issued_at, refresh_expires_at, last_seen_at 저장
   - device_id, user_agent, ip 저장 가능

9. 트랜잭션을 commit 한다.

10. 서버 자체 access token / refresh token을 클라이언트에 반환한다.


user_id의 경우는 서버에서 직접 만드는 방향으로 가야할듯 이유는 

sub는 Unity UGS 쪽의 외부 식별자입니다.
지금은 UGS만 쓰더라도 나중에 Apple, Google, Guest, Steam, 
자체 계정, 계정 이전, 계정 병합이 들어오면 sub = 유저 ID 구조가 바로 부담이 됩니다.

현재 이 검증 규칙 설계를 기반으로 구현을 진행중입니다.
