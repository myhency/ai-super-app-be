
// =============================================================================
// Web Worker 방식으로 SSE 백그라운드 처리
// =============================================================================

// 1. Worker 파일 생성 (public/sse-worker.js)
// Web Worker는 메인 UI 스레드와 독립적으로 실행되어 
// 페이지 이동이나 탭 전환에도 영향받지 않음

// sse-worker.js 파일 내용:
// -----------------------------------------------------------------------------
/*
// Worker 내부에서 각 topicId별 SSE 연결을 Map으로 관리
self.connections = new Map();

// 메인 스레드로부터 메시지 수신 처리
self.addEventListener('message', (e) => {
  const { type, topicId, url } = e.data;
  
  switch(type) {
    case 'START_SSE':
      // 새로운 SSE 연결 시작
      const eventSource = new EventSource(url);
      
      // 서버로부터 메시지 수신 시
      eventSource.onmessage = (event) => {
        // 메인 스레드에 데이터 전송
        self.postMessage({
          type: 'MESSAGE',
          topicId,
          data: JSON.parse(event.data)
        });
      };
      
      // 스트림 종료 시 (서버에서 'end' 이벤트 발송)
      eventSource.addEventListener('end', () => {
        // 메인 스레드에 완료 알림
        self.postMessage({
          type: 'COMPLETE',
          topicId
        });
        
        // 연결 정리
        eventSource.close();
        self.connections.delete(topicId);
      });
      
      // 에러 처리
      eventSource.onerror = (error) => {
        self.postMessage({
          type: 'ERROR',
          topicId,
          error: error.message
        });
      };
      
      // 연결 저장
      self.connections.set(topicId, eventSource);
      break;
      
    case 'CLOSE_SSE':
      // 특정 topicId 연결 종료
      const conn = self.connections.get(topicId);
      if (conn) {
        conn.close();
        self.connections.delete(topicId);
      }
      break;
      
    case 'CLOSE_ALL':
      // 모든 연결 종료 (앱 종료 시)
      self.connections.forEach((conn, topicId) => {
        conn.close();
      });
      self.connections.clear();
      break;
  }
});
*/

// 2. React Hook으로 Worker 관리
// -----------------------------------------------------------------------------
import { useEffect, useState, useRef, useCallback } from 'react';

export function useSSEWorker() {
  const [worker, setWorker] = useState(null);
  const [activeTopics, setActiveTopics] = useState(new Set());
  const messageHandlers = useRef(new Map()); // topicId -> handler function
  
  useEffect(() => {
    // Worker 초기화
    const sseWorker = new Worker('/sse-worker.js');
    
    // Worker로부터 메시지 수신 처리
    sseWorker.onmessage = (e) => {
      const { type, topicId, data } = e.data;
      
      switch(type) {
        case 'MESSAGE':
          // 해당 topic의 메시지 핸들러 실행
          const handler = messageHandlers.current.get(topicId);
          if (handler) {
            handler(data);
          }
          break;
          
        case 'COMPLETE':
          // 스트림 완료 - 브라우저 알림 표시
          if (Notification.permission === "granted") {
            new Notification("AI 응답 완료", {
              body: `대화 ${topicId}의 응답이 완료되었습니다`,
              icon: "/chat-icon.png",
              tag: topicId,
              requireInteraction: true,
              actions: [
                { action: 'view', title: '대화 보기' }
              ]
            }).onclick = () => {
              // 해당 topic 페이지로 이동
              window.location.href = `/chat/${topicId}`;
            };
          }
          
          // 활성 topic 목록에서 제거
          setActiveTopics(prev => {
            const newSet = new Set(prev);
            newSet.delete(topicId);
            return newSet;
          });
          
          // 핸들러 정리
          messageHandlers.current.delete(topicId);
          break;
          
        case 'ERROR':
          console.error(`SSE Error for topic ${topicId}:`, data.error);
          break;
      }
    };
    
    setWorker(sseWorker);
    
    // 알림 권한 요청
    if (Notification.permission === "default") {
      Notification.requestPermission();
    }
    
    // 정리 함수 - 앱 종료 시 모든 연결 정리
    return () => {
      sseWorker.postMessage({ type: 'CLOSE_ALL' });
      sseWorker.terminate();
    };
  }, []);
  
  // 새로운 SSE 연결 시작
  const startSSE = useCallback((topicId, onMessage) => {
    if (!worker) return;
    
    // 이미 연결이 있는 경우 스킵
    if (activeTopics.has(topicId)) {
      console.log(`Topic ${topicId} already active`);
      return;
    }
    
    // 메시지 핸들러 등록
    messageHandlers.current.set(topicId, onMessage);
    
    // Worker에 SSE 시작 명령
    worker.postMessage({
      type: 'START_SSE',
      topicId,
      url: `/v1/chat?topicId=${topicId}`
    });
    
    // 활성 topic 목록에 추가
    setActiveTopics(prev => new Set([...prev, topicId]));
  }, [worker, activeTopics]);
  
  // 특정 SSE 연결 종료
  const stopSSE = useCallback((topicId) => {
    if (!worker) return;
    
    worker.postMessage({
      type: 'CLOSE_SSE',
      topicId
    });
    
    // 상태 정리
    setActiveTopics(prev => {
      const newSet = new Set(prev);
      newSet.delete(topicId);
      return newSet;
    });
    messageHandlers.current.delete(topicId);
  }, [worker]);
  
  return { 
    startSSE, 
    stopSSE, 
    activeTopics: Array.from(activeTopics),
    isWorkerReady: !!worker 
  };
}

// 3. 실제 Chat 컴포넌트에서 사용
// -----------------------------------------------------------------------------
export function ChatPage({ topicId }) {
  const { startSSE, stopSSE, activeTopics, isWorkerReady } = useSSEWorker();
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  
  // 메시지 전송 함수
  const sendMessage = useCallback(async (content) => {
    if (!isWorkerReady) return;
    
    setIsLoading(true);
    
    // 메시지 핸들러 정의 - 스트리밍 메시지 수신 시 실행
    const handleMessage = (data) => {
      setMessages(prev => {
        // 마지막 메시지가 AI 응답인 경우 업데이트, 아니면 새로 추가
        const lastMsg = prev[prev.length - 1];
        if (lastMsg && lastMsg.role === 'assistant' && !lastMsg.completed) {
          return [
            ...prev.slice(0, -1),
            { ...lastMsg, content: lastMsg.content + data.content }
          ];
        } else {
          return [...prev, { role: 'assistant', content: data.content, completed: false }];
        }
      });
    };
    
    try {
      // 사용자 메시지 추가
      setMessages(prev => [...prev, { role: 'user', content, completed: true }]);
      
      // Worker에서 SSE 연결 시작
      startSSE(topicId, handleMessage);
      
      // 서버에 메시지 전송 (일반 HTTP POST)
      await fetch('/v1/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          topicUlid: topicId, 
          content 
        })
      });
      
    } catch (error) {
      console.error('Message send failed:', error);
      setMessages(prev => [...prev, { 
        role: 'error', 
        content: '메시지 전송 실패', 
        completed: true 
      }]);
    } finally {
      setIsLoading(false);
    }
  }, [topicId, startSSE, isWorkerReady]);
  
  // 컴포넌트 언마운트 시에도 Worker 연결은 유지됨
  // 사용자가 다른 페이지로 이동해도 백그라운드에서 응답 수신 가능
  
  return (
    <div className="chat-container">
      <div className="chat-header">
        <h2>Chat Topic: {topicId}</h2>
        
        {/* 백그라운드 대화 상태 표시 */}
        {activeTopics.length > 1 && (
          <div className="background-indicator">
            백그라운드 대화 진행 중: 
            {activeTopics.filter(id => id !== topicId).length}개
          </div>
        )}
      </div>
      
      <div className="messages">
        {messages.map((msg, i) => (
          <div key={i} className={`message message-${msg.role}`}>
            {msg.content}
          </div>
        ))}
        {isLoading && <div className="loading">응답 대기 중...</div>}
      </div>
      
      <div className="input-area">
        <button 
          onClick={() => sendMessage("안녕하세요")}
          disabled={isLoading || !isWorkerReady}
        >
          메시지 보내기
        </button>
      </div>
    </div>
  );
}

// 4. 전체 앱에서 Worker 활용
// -----------------------------------------------------------------------------
export function App() {
  const { activeTopics } = useSSEWorker();
  
  return (
    <div>
      {/* 글로벌 알림 영역 */}
      {activeTopics.length > 0 && (
        <div className="global-notification">
          진행 중인 대화: {activeTopics.length}개
        </div>
      )}
      
      {/* 라우터 */}
      <Router>
        <Routes>
          <Route path="/chat/:topicId" element={<ChatPage />} />
        </Routes>
      </Router>
    </div>
  );
}