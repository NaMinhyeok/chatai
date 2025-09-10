# AI 챗봇 서비스 요구사항

## 프로젝트 개요

VIP onboarding 팀을 위한 AI 챗봇 API 서비스 개발  
고객사 시연을 위한 3시간 내 MVP 구현

## 비즈니스 요구사항

- 챗봇 AI를 사용할 수 있는 API 제공
- 향후 대외비 문서 학습 확장 가능
- OpenAI 등 표준 API spec 호환
- 지속적인 확장 개발 가능한 구조

## 1. 사용자 관리 및 인증 기능

### 데이터 모델

**User (사용자)**
- email: String (필수, 유니크)
- password: String (필수, 해시화 저장)
- name: String (필수)
- role: String(ENUM) ("member" | "admin")
- created_at: Timestamp

### 구현 기능

#### 회원가입
- **Endpoint**: `POST /auth/register`
- **필수 입력**: 이메일, 패스워드, 이름
- **응답**: 성공/실패 상태

#### 로그인
- **Endpoint**: `POST /auth/login`
- **필수 입력**: 이메일, 패스워드
- **응답**: JWT 토큰 발급

#### 인증
- 회원가입/로그인 외 모든 요청에 JWT 토큰 필수
- Bearer Token 방식 사용

## 2. 대화(Chat) 관리 기능

### 데이터 모델

**Thread (스레드)**
- id: PK
- user_id: Foreign Key → User
- created_at: Timestamp
- updated_at: Timestamp (마지막 대화 시간)

**Chat (대화)**
- id: PK
- thread_id: Foreign Key → Thread
- question: String (필수)
- answer: String (필수)
- created_at: Timestamp

### 스레드 관리 규칙

1. **새 스레드 생성 조건**
   - 해당 유저의 첫 질문
   - 마지막 질문 후 30분 경과 시점에 새 질문

2. **기존 스레드 유지 조건**
   - 마지막 질문 후 30분 이내 재질문

### 구현 기능

#### 질문 및 답변
- **Endpoint**: `POST /chat/message`
- **기능**: 
  - 스레드 자동 관리 (생성/연결)
  - OpenAI API 호출
  - 대화 히스토리 포함하여 요청
  - 답변 저장 및 반환

#### 스레드 목록 조회
- **Endpoint**: `GET /chat/threads`
- **기능**: 사용자의 모든 스레드 목록 반환

#### 대화 내역 조회
- **Endpoint**: `GET /chat/threads/:threadId/messages`
- **기능**: 특정 스레드의 전체 대화 내역 반환

## 3. OpenAI API 연동

### 요청 형식
```json
{
  "model": "gpt-3.5-turbo",
  "messages": [
    {"role": "user", "content": "Who won the world series in 2020?"},
    {"role": "assistant", "content": "The Los Angeles Rams won the Super Bowl in 2022."},
    {"role": "user", "content": "Where was it played?"}
  ]
}
```

### 처리 플로우
1. 스레드 확인/생성
2. 해당 스레드의 대화 히스토리 조회
3. OpenAI API 요청 (히스토리 + 새 질문)
4. 응답 저장
5. 클라이언트에 답변 반환

## 4. 기술 요구사항

### 보안
- 패스워드 해싱 (bcrypt)
- JWT 토큰 검증
- API 인증 미들웨어

### 데이터베이스
- 관계형 DB (PostgreSQL 15.8+)
- 외래키 제약조건
- 인덱스 최적화

### API 설계
- RESTful API
- 표준 HTTP 상태코드
- JSON 형식 요청/응답

## 5. 확장성 고려사항

- 모듈화된 구조
- 환경변수를 통한 설정 관리
- 로깅 시스템
- 에러 핸들링
- API 버전 관리 준비

## 6. 개발 우선순위

1. 기본 프로젝트 구조 설정
2. 데이터베이스 스키마 구현
3. 회원가입/로그인 기능
4. 기본 대화 기능
5. OpenAI API 연동
6. 스레드 관리 로직
7. JWT를 활용한 인증