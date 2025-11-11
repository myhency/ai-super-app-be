# CORS 설정 가이드

프론트엔드(`localhost:3000`)에서 백엔드(`localhost:8080`)로 SSE 및 HTTP 요청을 보내기 위해 CORS 설정이 필요합니다.

## 🚨 백엔드 CORS 설정 필요

### Spring Boot에서 CORS 설정

#### 1. 전역 CORS 설정
```java
// src/main/java/io/hency/aisuperapp/config/WebConfig.java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v1/**")
                .allowedOrigins("http://localhost:3000")  // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

#### 2. 또는 @CrossOrigin 애노테이션 사용
```java
// ChatController.java에 추가
@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/v1/chat")
public class ChatController {
    // ... 기존 코드
}
```

#### 3. 또는 CORS Filter 설정
```java
@Component
public class CorsFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }
}
```

## 🔧 SSE 특화 CORS 설정

SSE는 특별한 헤더가 필요할 수 있습니다:

```java
@PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
@CrossOrigin(origins = "http://localhost:3000")
public Flux<Chat> sendMessage(@Valid @RequestBody Mono<ChatRequest.SendMessageRequest> request) {
    return request
            .doOnNext(this::validate)
            .zipWith(UserContextHolder.getUserMono())
            .flatMapMany(this::createTopic)
            .flatMap(this::sendMessage);
}
```

## 🔍 CORS 문제 확인 방법

### 1. 브라우저 개발자 도구에서 확인
```
Network 탭 → 요청 확인 → Response Headers 확인:
- Access-Control-Allow-Origin: http://localhost:3000
- Access-Control-Allow-Methods: GET, POST, ...
- Access-Control-Allow-Headers: *
```

### 2. 일반적인 CORS 에러 메시지
```
Access to fetch at 'http://localhost:8080/v1/chat' from origin 'http://localhost:3000' 
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present 
on the requested resource.
```

### 3. SSE 관련 에러
```
EventSource's response has a MIME type ("application/json") that is not "text/event-stream". 
Aborting the connection.
```

## 🚀 테스트 방법

### 1. 프론트엔드 시작
```bash
cd frontend-demo
npm start
# http://localhost:3000에서 실행
```

### 2. 백엔드 시작
```bash
# Spring Boot 앱 실행
# http://localhost:8080에서 실행
```

### 3. CORS 테스트
```bash
# 브라우저에서 F12 → Console에서 테스트
fetch('http://localhost:8080/v1/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ topicUlid: 'test', content: 'hello' })
})
.then(response => console.log('Success:', response))
.catch(error => console.error('CORS Error:', error));
```

## 🔧 프록시 대안 (개발용)

CORS 설정이 어렵다면 프록시 서버 사용:

### 1. Vite 설정 (vite.config.js)
```javascript
export default {
  server: {
    port: 3000,
    proxy: {
      '/v1': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
```

### 2. 프론트엔드 코드에서 상대 경로 사용
```javascript
// useSSEWorker.js에서
const url = `/v1/chat?topicId=${topicId}`;  // 프록시 사용

// ChatManager.js에서
const response = await fetch('/v1/chat', {  // 프록시 사용
  method: 'POST',
  // ...
});
```

## 📝 체크리스트

- [ ] 백엔드에 CORS 설정 추가
- [ ] SSE 엔드포인트에 CORS 허용
- [ ] 프론트엔드에서 올바른 URL 사용
- [ ] 브라우저에서 CORS 에러 없이 요청 성공
- [ ] SSE 연결이 정상적으로 동작
- [ ] 알림 권한 허용 확인

---

이 설정 완료 후 `npm start`로 프론트엔드를 실행하면 백엔드와 정상 연동됩니다!