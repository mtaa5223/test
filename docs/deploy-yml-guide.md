# deploy.yml 상세 가이드

## 개요

`deploy.yml`은 GitHub Actions 워크플로우 파일로, PR이 머지되면 자동으로 Docker 이미지를 빌드하고 GCP Cloud Run에 배포한다.

## 트리거 조건

```yaml
on:
  push:
    branches: [main, develop]
```

- `develop` 또는 `main` 브랜치에 push(= PR 머지)될 때 실행된다.
- 다른 브랜치에서는 실행되지 않는다.

## 전역 환경변수

```yaml
env:
  REGISTRY: asia-northeast3-docker.pkg.dev
  PROJECT_ID: helical-ion-460310-m0
  REPOSITORY: test
  IMAGE_NAME: trinity-server
  REGION: asia-northeast3
```

| 변수 | 값 | 용도 |
|------|-----|------|
| `REGISTRY` | `asia-northeast3-docker.pkg.dev` | GCP Artifact Registry 주소 |
| `PROJECT_ID` | `helical-ion-460310-m0` | GCP 프로젝트 ID |
| `REPOSITORY` | `test` | Artifact Registry 저장소 이름 |
| `IMAGE_NAME` | `trinity-server` | Docker 이미지 이름 |
| `REGION` | `asia-northeast3` | Cloud Run 배포 리전 (서울) |

이 값들로 조합된 최종 이미지 경로:
```
asia-northeast3-docker.pkg.dev/helical-ion-460310-m0/test/trinity-server
```

---

## Job 0: validate (사전 검증)

빌드를 시작하기 전에 필수 조건을 검사한다. 실패하면 이후 모든 작업이 중단된다.

### Step: Check required secrets

GitHub에 등록된 Secrets가 존재하는지 확인한다.

검사 대상:
- `GCP_SA_KEY` — GCP 서비스 계정 JSON 키
- `DEV_DB_URL` / `PROD_DB_URL` — 데이터베이스 접속 URL
- `DEV_DB_USER` / `PROD_DB_USER` — 데이터베이스 사용자
- `DEV_DB_PASSWORD` / `PROD_DB_PASSWORD` — 데이터베이스 비밀번호

하나라도 빠져있으면 어떤 Secret이 없는지 에러 메시지로 알려주고 중단한다.

### Step: Check required tools

빌드 컴퓨터(self-hosted runner)에 필수 도구가 설치되어 있는지 확인한다.

검사 대상:
- `docker` — Docker CLI
- `gcloud` — Google Cloud CLI

설치되어 있으면 버전 정보를 출력하고, 없으면 에러로 중단한다.

---

## Job 1: build (빌드 + Registry push)

`validate`가 성공한 후에 실행된다. develop과 main 공통으로 사용한다.

### Step: Checkout

```yaml
uses: actions/checkout@v4
with:
  fetch-depth: 0
```

- GitHub repo의 소스 코드를 빌드 컴퓨터에 가져온다.
- `fetch-depth: 0`은 전체 git 히스토리를 가져온다는 의미로, `git describe --tags`로 버전 태그를 읽기 위해 필요하다.

### Step: Set image tags and environment

브랜치에 따라 Docker 이미지 태그와 Cloud Run 서비스 이름을 결정한다.

#### develop 브랜치인 경우

| 생성되는 태그 | 예시 | 용도 |
|--------------|------|------|
| `{sha}` | `abc1234` | 특정 커밋의 빌드를 추적 |
| `dev-{sha}` | `dev-abc1234` | dev 빌드임을 명시 |
| `dev-latest` | `dev-latest` | develop의 최신 이미지 |

- Cloud Run 서비스 이름: `trinity-server-dev`

#### main 브랜치인 경우

| 생성되는 태그 | 예시 | 용도 |
|--------------|------|------|
| `{sha}` | `abc1234` | 특정 커밋의 빌드를 추적 |
| `latest` | `latest` | main의 최신 이미지 |
| `{version}` | `v1.0.0` | git tag가 있을 때만 생성 |

- Cloud Run 서비스 이름: `trinity-server-prod`
- 버전 태그는 `git describe --tags --abbrev=0`으로 가장 가까운 git tag를 찾아서 붙인다. tag가 없으면 생략된다.

### Step: Authenticate to GCP

```yaml
uses: google-github-actions/auth@v2
with:
  credentials_json: ${{ secrets.GCP_SA_KEY }}
```

GitHub Secret에 저장된 서비스 계정 JSON 키로 GCP에 인증한다. 이후 `gcloud`와 `docker push` 명령이 GCP에 접근할 수 있게 된다.

### Step: Configure Docker for Artifact Registry

```bash
gcloud auth configure-docker asia-northeast3-docker.pkg.dev --quiet
```

Docker CLI가 GCP Artifact Registry에 이미지를 push할 수 있도록 인증을 연결한다. `--quiet`는 확인 프롬프트를 건너뛴다.

### Step: Build Docker image

앞서 결정한 태그들을 모두 붙여서 Docker 이미지를 빌드한다.

실제 실행되는 명령 예시 (develop):
```bash
docker build -t .../trinity-server:abc1234 -t .../trinity-server:dev-abc1234 -t .../trinity-server:dev-latest .
```

Dockerfile의 동작:
1. JDK 21 이미지에서 `gradlew installDist`로 앱을 빌드
2. JRE 21 이미지에 빌드 결과물만 복사 (이미지 크기 최소화)

### Step: Push Docker image

빌드된 이미지의 모든 태그를 GCP Artifact Registry에 업로드한다.

### Step: Clean up old Docker images

```yaml
if: always()
```

빌드 성공/실패와 관계없이 항상 실행된다. 빌드 컴퓨터에 쌓이는 오래된 Docker 이미지를 삭제하여 디스크 공간을 확보한다.

---

## Job 2: deploy-dev (develop 자동 배포)

```yaml
needs: build
if: github.ref_name == 'develop'
```

- `build` job이 성공한 후에만 실행된다.
- develop 브랜치일 때만 실행된다.
- **승인 없이 자동으로 배포된다.**

### Step: Deploy to Cloud Run (dev)

```yaml
uses: google-github-actions/deploy-cloudrun@v2
with:
  service: trinity-server-dev
  image: .../trinity-server:abc1234
  region: asia-northeast3
  env_vars: |
    APP_ENV=develop
    DB_URL=...
    ...
```

- Cloud Run 서비스 `trinity-server-dev`에 새 이미지로 배포한다.
- GitHub Secrets에 저장된 dev 환경변수를 Cloud Run에 주입한다.
- 배포할 때마다 환경변수가 덮어쓰여지므로, 환경변수 변경 시 GitHub Secrets만 수정하면 된다.

#### 주입되는 환경변수

| 환경변수 | Secret 이름 | 설명 |
|----------|------------|------|
| `APP_ENV` | (고정값 `develop`) | 앱 실행 환경 |
| `DB_URL` | `DEV_DB_URL` | 데이터베이스 접속 URL |
| `DB_USER` | `DEV_DB_USER` | 데이터베이스 사용자 |
| `DB_PASSWORD` | `DEV_DB_PASSWORD` | 데이터베이스 비밀번호 |
| `DB_POOL_SIZE` | `DEV_DB_POOL_SIZE` | DB 커넥션 풀 크기 |
| `UGS_PROJECT_ID` | `UGS_PROJECT_ID` | Unity Gaming Services 프로젝트 ID |
| `UGS_ISSUER` | `UGS_ISSUER` | UGS JWT 발급자 |
| `UGS_JWKS_URL` | `UGS_JWKS_URL` | UGS JWT 공개키 URL |

---

## Job 3: deploy-prod (main 수동 승인 배포)

```yaml
needs: build
if: github.ref_name == 'main'
environment: production
```

- `build` job이 성공한 후에만 실행된다.
- main 브랜치일 때만 실행된다.
- **`environment: production` 설정으로 지정된 승인자가 Approve해야 배포가 진행된다.**

### 승인 설정 방법 (GitHub에서 1회 설정)

1. GitHub repo → Settings → Environments
2. New environment → 이름: `production`
3. Required reviewers 체크 → 승인할 사람 추가
4. Save

### Step: Deploy to Cloud Run (prod)

dev와 동일한 방식이지만, prod 전용 환경변수를 사용한다.

| 환경변수 | Secret 이름 | 설명 |
|----------|------------|------|
| `APP_ENV` | (고정값 `production`) | 앱 실행 환경 |
| `DB_URL` | `PROD_DB_URL` | 프로덕션 데이터베이스 접속 URL |
| `DB_USER` | `PROD_DB_USER` | 프로덕션 데이터베이스 사용자 |
| `DB_PASSWORD` | `PROD_DB_PASSWORD` | 프로덕션 데이터베이스 비밀번호 |
| `DB_POOL_SIZE` | `PROD_DB_POOL_SIZE` | 프로덕션 DB 커넥션 풀 크기 |
| `UGS_PROJECT_ID` | `UGS_PROJECT_ID` | Unity Gaming Services 프로젝트 ID |
| `UGS_ISSUER` | `UGS_ISSUER` | UGS JWT 발급자 |
| `UGS_JWKS_URL` | `UGS_JWKS_URL` | UGS JWT 공개키 URL |

---

## 전체 실행 흐름 요약

```
develop 머지 시:
  validate → build → deploy-dev (자동)

main 머지 시:
  validate → build → deploy-prod (승인 대기 → 승인 후 배포)
```

## GitHub Secrets 전체 목록

| Secret 이름 | 용도 | 필수 |
|------------|------|:----:|
| `GCP_SA_KEY` | GCP 서비스 계정 JSON 키 | O |
| `DEV_DB_URL` | dev DB 접속 URL | O |
| `DEV_DB_USER` | dev DB 사용자 | O |
| `DEV_DB_PASSWORD` | dev DB 비밀번호 | O |
| `DEV_DB_POOL_SIZE` | dev DB 풀 크기 | O |
| `PROD_DB_URL` | prod DB 접속 URL | O |
| `PROD_DB_USER` | prod DB 사용자 | O |
| `PROD_DB_PASSWORD` | prod DB 비밀번호 | O |
| `PROD_DB_POOL_SIZE` | prod DB 풀 크기 | O |
| `UGS_PROJECT_ID` | UGS 프로젝트 ID | O |
| `UGS_ISSUER` | UGS JWT 발급자 | O |
| `UGS_JWKS_URL` | UGS JWT 공개키 URL | O |
