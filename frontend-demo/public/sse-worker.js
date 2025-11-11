// SSE Worker - 백그라운드에서 여러 topic의 SSE 연결 관리
console.log('SSE Worker 시작됨');

// 각 topicId별 SSE 연결을 저장하는 Map
self.connections = new Map();

// 메인 스레드로부터 메시지 수신
self.addEventListener('message', (e) => {
  const { type, topicId, url, headers } = e.data;
  console.log(`Worker 수신: ${type}, Topic: ${topicId}`);
  
  switch(type) {
    case 'START_SSE':
      startSSEConnection(topicId, url, headers);
      break;
      
    case 'CLOSE_SSE':
      closeSSEConnection(topicId);
      break;
      
    case 'CLOSE_ALL':
      closeAllConnections();
      break;
      
    default:
      console.warn('알 수 없는 메시지 타입:', type);
  }
});

// 새로운 SSE 연결 시작
function startSSEConnection(topicId, url, headers = {}) {
  // 이미 연결이 있으면 기존 연결 종료
  if (self.connections.has(topicId)) {
    console.log(`기존 연결 발견, 종료 후 재시작: ${topicId}`);
    closeSSEConnection(topicId);
  }
  
  try {
    // EventSource는 직접적으로 커스텀 헤더를 지원하지 않으므로
    // Authorization 토큰을 URL 쿼리 파라미터로 전달
    let finalUrl = url;
    if (headers && headers.Authorization) {
      const separator = url.includes('?') ? '&' : '?';
      const token = headers.Authorization.replace('Bearer ', '');
      finalUrl = `${url}${separator}token=${encodeURIComponent(token)}`;
      console.log(`인증 토큰 포함하여 SSE 연결: ${topicId}`, finalUrl);
    } else {
      console.warn(`인증 토큰이 없습니다: ${topicId}`);
    }
    
    // EventSource로 SSE 연결 생성
    const eventSource = new EventSource(finalUrl);
    console.log(`SSE 연결 시작: ${topicId} -> ${finalUrl}`);
    
    // 연결 성공
    eventSource.onopen = () => {
      console.log(`SSE 연결 성공: ${topicId}`);
      self.postMessage({
        type: 'CONNECTION_OPENED',
        topicId
      });
    };
    
    // 메시지 수신 (스트리밍 데이터)
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log(`메시지 수신: ${topicId}`, data);
        
        // 메인 스레드에 데이터 전달
        self.postMessage({
          type: 'MESSAGE',
          topicId,
          data: data
        });
      } catch (parseError) {
        console.error('JSON 파싱 에러:', parseError);
        // 텍스트 그대로 전달 - Chat 객체 형식으로 래핑
        self.postMessage({
          type: 'MESSAGE',
          topicId,
          data: { 
            message: { 
              content: event.data, 
              role: 'AI' 
            }, 
            type: 'text' 
          }
        });
      }
    };
    
    // 스트림 종료 이벤트 (서버에서 명시적으로 보내는 경우)
    eventSource.addEventListener('end', () => {
      console.log(`스트림 종료: ${topicId}`);
      handleStreamComplete(topicId);
    });
    
    // 사용자 정의 이벤트 처리 (필요시)
    eventSource.addEventListener('chunk', (event) => {
      try {
        const data = JSON.parse(event.data);
        self.postMessage({
          type: 'CHUNK',
          topicId,
          data: data
        });
      } catch (e) {
        console.error('Chunk 파싱 에러:', e);
      }
    });
    
    // 에러 처리
    eventSource.onerror = (error) => {
      console.error(`SSE 에러: ${topicId}`, error);
      
      // 연결 상태 확인
      if (eventSource.readyState === EventSource.CLOSED) {
        console.log(`연결 종료됨: ${topicId}`);
        handleStreamComplete(topicId);
      } else if (eventSource.readyState === EventSource.CONNECTING) {
        console.log(`재연결 시도 중: ${topicId}`);
        self.postMessage({
          type: 'RECONNECTING',
          topicId
        });
      } else {
        // 일반적인 에러
        self.postMessage({
          type: 'ERROR',
          topicId,
          error: error.message || 'SSE 연결 에러'
        });
      }
    };
    
    // 연결 저장
    self.connections.set(topicId, eventSource);
    
  } catch (error) {
    console.error(`SSE 연결 생성 실패: ${topicId}`, error);
    self.postMessage({
      type: 'ERROR',
      topicId,
      error: error.message
    });
  }
}

// 스트림 완료 처리
function handleStreamComplete(topicId) {
  // 메인 스레드에 완료 알림
  self.postMessage({
    type: 'COMPLETE',
    topicId,
    timestamp: new Date().toISOString()
  });
  
  // 연결 정리
  closeSSEConnection(topicId);
}

// 특정 SSE 연결 종료
function closeSSEConnection(topicId) {
  const connection = self.connections.get(topicId);
  if (connection) {
    console.log(`SSE 연결 종료: ${topicId}`);
    connection.close();
    self.connections.delete(topicId);
    
    self.postMessage({
      type: 'CONNECTION_CLOSED',
      topicId
    });
  }
}

// 모든 SSE 연결 종료 (앱 종료 시)
function closeAllConnections() {
  console.log(`모든 SSE 연결 종료 (총 ${self.connections.size}개)`);
  
  self.connections.forEach((connection, topicId) => {
    connection.close();
    console.log(`연결 종료: ${topicId}`);
  });
  
  self.connections.clear();
  
  self.postMessage({
    type: 'ALL_CONNECTIONS_CLOSED',
    count: self.connections.size
  });
}

// Worker 종료 시 정리
self.addEventListener('beforeunload', () => {
  closeAllConnections();
});

// 에러 핸들링
self.addEventListener('error', (error) => {
  console.error('Worker 에러:', error);
  self.postMessage({
    type: 'WORKER_ERROR',
    error: error.message
  });
});

console.log('SSE Worker 초기화 완료');