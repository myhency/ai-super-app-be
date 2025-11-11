# Multi-Topic SSE Chat Demo

Web Worker를 활용한 백그라운드 SSE 연결 관리 데모입니다.

## 🎯 핵심 기능

- **다중 Topic 동시 관리**: 여러 대화를 동시에 진행
- **백그라운드 SSE 처리**: Web Worker로 페이지 전환에도 연결 유지
- **브라우저 알림**: 백그라운드 응답 완료 시 알림
- **실시간 상태 표시**: 각 Topic의 연결 상태 시각화

## 📁 프로젝트 구조

```
frontend-demo/
├── public/
│   ├── index.html          # 메인 HTML + 인라인 CSS
│   └── sse-worker.js       # Web Worker (SSE 연결 관리)
└── src/
    ├── hooks/
    │   └── useSSEWorker.js  # SSE Worker 관리 Hook
    └── components/
        ├── ChatManager.js   # 메인 컨테이너
        ├── TopicList.js     # Topic 목록 사이드바
        └── ChatRoom.js      # 개별 채팅방

```

## 🚀 실행 방법

### 1. 프론트엔드 실행 (포트 3000)
```bash
cd frontend-demo
npm install  # 최초 1회만
npm start    # http://localhost:3000에서 실행
```

### 2. 백엔드 실행 (포트 8080)
```bash
# Spring Boot 앱 실행
# http://localhost:8080에서 실행 필요
```

### 3. CORS 설정 필수
백엔드에 CORS 설정이 필요합니다. `CORS-SETUP.md` 파일을 참고하세요.

## 🔧 백엔드 연동

현재는 시뮬레이션 모드입니다. 실제 백엔드와 연동하려면:

### 1. `useSSEWorker.js` 수정
```javascript
// 실제 서버 URL로 변경
const url = `http://your-server.com/v1/chat?topicId=${topicId}`;
```

### 2. `ChatManager.js`에서 실제 HTTP 요청 활성화
```javascript
const simulateServerRequest = async (topicId, content) => {
  // 시뮬레이션 코드 제거하고 실제 구현 사용
  try {
    await fetch('/v1/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        topicUlid: topicId,
        content: content
      })
    });
  } catch (error) {
    console.error('메시지 전송 실패:', error);
  }
};
```

## 🎮 사용법

### 1. 새 대화 시작
- 좌측 사이드바에서 "새 대화" 버튼 클릭
- 자동으로 새 Topic이 생성됨

### 2. 메시지 전송
- 우측 채팅창에서 메시지 입력 후 전송
- 예시 메시지 버튼 클릭으로 빠른 테스트

### 3. 다중 대화 관리
- 여러 Topic에서 동시에 메시지 전송 가능
- 페이지 전환 시에도 백그라운드에서 응답 수신
- 응답 완료 시 브라우저 알림

### 4. 상태 모니터링
- 각 Topic의 연결 상태 실시간 확인
- 백그라운드 활동 상태 표시

## 🔍 동작 원리

### Web Worker 기반 SSE 관리
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  메인 스레드  │◄──►│ Web Worker   │◄──►│   서버       │
│  (React UI) │    │ (SSE 연결)   │    │  (백엔드)    │
└─────────────┘    └─────────────┘    └─────────────┘
```

1. **메인 스레드**: UI 렌더링 및 사용자 상호작용
2. **Web Worker**: SSE 연결 관리 (백그라운드)
3. **서버**: Spring Boot ChatController

### 페이지 전환 시나리오
1. Topic A에서 질문 → Worker가 SSE 연결 시작
2. 사용자가 Topic B로 이동 → A 연결은 Worker에서 유지
3. A 응답 완료 → Worker가 알림 발송 + A 페이지로 이동

## ⚙️ 설정 옵션

### 알림 설정
```javascript
// useSSEWorker.js에서 수정
const showCompletionNotification = (topicId) => {
  // 알림 내용 커스터마이징
  const notification = new Notification('커스텀 제목', {
    body: `커스텀 메시지 ${topicId}`,
    icon: '/custom-icon.png',
    // ...
  });
};
```

### 연결 타임아웃 설정
```javascript
// sse-worker.js에서 추가
eventSource.onerror = (error) => {
  // 재연결 로직 추가
  setTimeout(() => {
    // 재연결 시도
  }, 5000);
};
```

## 🐛 트러블슈팅

### Web Worker 로드 실패
- `sse-worker.js` 파일이 `/public` 경로에 있는지 확인
- CORS 정책으로 `file://` 프로토콜에서는 동작하지 않음

### 알림이 표시되지 않음
- 브라우저 알림 권한 허용 필요
- 개발자 도구에서 권한 상태 확인

### SSE 연결 실패
- 백엔드 서버가 실행 중인지 확인
- CORS 설정 확인
- 네트워크 탭에서 연결 상태 모니터링

## 📱 브라우저 호환성

- ✅ Chrome 60+
- ✅ Firefox 55+
- ✅ Safari 11+
- ✅ Edge 79+

## 🔗 관련 기술

- **Web Workers**: 백그라운드 스레드
- **Server-Sent Events**: 서버 → 클라이언트 스트리밍
- **Notification API**: 브라우저 알림
- **React Hooks**: 상태 관리
- **EventSource**: SSE 클라이언트

---

이 데모는 실제 프로덕션 환경에서 다중 채팅 세션을 백그라운드로 관리하는 방법을 보여줍니다.