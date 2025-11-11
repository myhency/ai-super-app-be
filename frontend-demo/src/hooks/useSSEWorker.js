// SSE Worker를 관리하는 React Hook
import { useEffect, useState, useRef, useCallback } from 'react';

export function useSSEWorker() {
  const [worker, setWorker] = useState(null);
  const [activeTopics, setActiveTopics] = useState(new Set());
  const [connectionStatus, setConnectionStatus] = useState({}); // topicId -> status
  const messageHandlers = useRef(new Map()); // topicId -> handler function
  const [isWorkerReady, setIsWorkerReady] = useState(false);
  
  useEffect(() => {
    console.log('SSE Worker 초기화 시작');
    
    // Worker 생성
    const sseWorker = new Worker('/sse-worker.js');
    
    // Worker로부터 메시지 수신 처리
    sseWorker.onmessage = (e) => {
      const { type, topicId, data, error, timestamp } = e.data;
      console.log(`Worker 메시지 수신: ${type}`, { topicId, data });
      
      switch(type) {
        case 'CONNECTION_OPENED':
          setConnectionStatus(prev => ({
            ...prev,
            [topicId]: 'connected'
          }));
          break;
          
        case 'MESSAGE':
        case 'CHUNK':
          console.log(`useSSEWorker - ${type} 수신:`, { topicId, data });
          // 해당 topic의 메시지 핸들러 실행
          const handler = messageHandlers.current.get(topicId);
          if (handler) {
            console.log(`useSSEWorker - 핸들러 실행:`, topicId);
            handler(data);
          } else {
            console.warn(`핸들러가 없는 topic: ${topicId}`);
            console.log('현재 등록된 핸들러들:', Array.from(messageHandlers.current.keys()));
          }
          break;
          
        case 'COMPLETE':
          console.log(`스트림 완료: ${topicId} at ${timestamp}`);
          
          // 완료 핸들러가 있으면 실행
          const completeHandler = messageHandlers.current.get(`${topicId}_complete`);
          if (completeHandler) {
            completeHandler();
          }
          
          // 브라우저 알림 표시
          showCompletionNotification(topicId);
          
          // 상태 업데이트
          setActiveTopics(prev => {
            const newSet = new Set(prev);
            newSet.delete(topicId);
            return newSet;
          });
          
          setConnectionStatus(prev => ({
            ...prev,
            [topicId]: 'completed'
          }));
          
          // 핸들러 정리
          messageHandlers.current.delete(topicId);
          messageHandlers.current.delete(`${topicId}_error`);
          messageHandlers.current.delete(`${topicId}_complete`);
          break;
          
        case 'CONNECTION_CLOSED':
          setConnectionStatus(prev => ({
            ...prev,
            [topicId]: 'closed'
          }));
          break;
          
        case 'RECONNECTING':
          setConnectionStatus(prev => ({
            ...prev,
            [topicId]: 'reconnecting'
          }));
          break;
          
        case 'ERROR':
          console.error(`SSE 에러: ${topicId}`, error);
          setConnectionStatus(prev => ({
            ...prev,
            [topicId]: 'error'
          }));
          
          // 에러 핸들러가 있으면 실행
          const errorHandler = messageHandlers.current.get(`${topicId}_error`);
          if (errorHandler) {
            errorHandler(error);
          }
          break;
          
        case 'WORKER_ERROR':
          console.error('Worker 에러:', error);
          break;
          
        default:
          console.log(`처리되지 않은 메시지 타입: ${type}`);
      }
    };
    
    sseWorker.onerror = (error) => {
      console.error('Worker 에러:', error);
      setIsWorkerReady(false);
    };
    
    setWorker(sseWorker);
    setIsWorkerReady(true);
    
    // 알림 권한 요청
    requestNotificationPermission();
    
    // 정리 함수 - 앱 종료 시 모든 연결 정리
    return () => {
      console.log('Worker 정리 시작');
      if (sseWorker) {
        sseWorker.postMessage({ type: 'CLOSE_ALL' });
        sseWorker.terminate();
      }
      setIsWorkerReady(false);
    };
  }, []);
  
  // 알림 권한 요청
  const requestNotificationPermission = async () => {
    if ('Notification' in window && Notification.permission === 'default') {
      try {
        const permission = await Notification.requestPermission();
        console.log('알림 권한:', permission);
      } catch (error) {
        console.error('알림 권한 요청 실패:', error);
      }
    }
  };
  
  // 완료 알림 표시
  const showCompletionNotification = (topicId) => {
    if ('Notification' in window && Notification.permission === 'granted') {
      const notification = new Notification('AI 응답 완료', {
        body: `대화 ${topicId}의 응답이 완료되었습니다. 클릭하여 확인하세요.`,
        icon: '/chat-icon.png',
        tag: topicId,
        requireInteraction: true,
        actions: [
          { action: 'view', title: '대화 보기' },
          { action: 'dismiss', title: '닫기' }
        ]
      });
      
      notification.onclick = () => {
        // 해당 topic 페이지로 이동
        window.focus();
        window.location.hash = `#/chat/${topicId}`;
        notification.close();
      };
      
      // 5초 후 자동 닫기
      setTimeout(() => {
        notification.close();
      }, 5000);
    }
  };
  
  // 새로운 SSE 연결 시작
  const startSSE = useCallback((topicId, onMessage, onError, onComplete) => {
    if (!worker || !isWorkerReady) {
      console.warn('Worker가 준비되지 않음');
      return false;
    }
    
    if (activeTopics.has(topicId)) {
      console.log(`Topic ${topicId} 이미 활성화됨`);
      return false;
    }
    
    console.log(`SSE 시작: ${topicId}`);
    
    // 메시지 핸들러 등록
    if (onMessage) {
      messageHandlers.current.set(topicId, onMessage);
    }
    
    // 에러 핸들러 등록
    if (onError) {
      messageHandlers.current.set(`${topicId}_error`, onError);
    }
    
    // 완료 핸들러 등록
    if (onComplete) {
      messageHandlers.current.set(`${topicId}_complete`, onComplete);
    }
    
    // Worker에 SSE 시작 명령 - 백엔드 서버 URL
    const url = `http://localhost:8080/v1/chat?topicId=${topicId}`;
    worker.postMessage({
      type: 'START_SSE',
      topicId,
      url
    });
    
    // 활성 topic 목록에 추가
    setActiveTopics(prev => new Set([...prev, topicId]));
    setConnectionStatus(prev => ({
      ...prev,
      [topicId]: 'connecting'
    }));
    
    return true;
  }, [worker, isWorkerReady, activeTopics]);
  
  // 특정 SSE 연결 종료
  const stopSSE = useCallback((topicId) => {
    if (!worker) return;
    
    console.log(`SSE 중지: ${topicId}`);
    
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
    
    setConnectionStatus(prev => ({
      ...prev,
      [topicId]: 'stopped'
    }));
    
    messageHandlers.current.delete(topicId);
    messageHandlers.current.delete(`${topicId}_error`);
  }, [worker]);
  
  // 모든 연결 종료
  const stopAllSSE = useCallback(() => {
    if (!worker) return;
    
    console.log('모든 SSE 연결 중지');
    
    worker.postMessage({ type: 'CLOSE_ALL' });
    
    setActiveTopics(new Set());
    setConnectionStatus({});
    messageHandlers.current.clear();
  }, [worker]);
  
  // 특정 topic의 상태 조회
  const getTopicStatus = useCallback((topicId) => {
    return connectionStatus[topicId] || 'disconnected';
  }, [connectionStatus]);
  
  return { 
    startSSE, 
    stopSSE,
    stopAllSSE,
    activeTopics: Array.from(activeTopics),
    connectionStatus,
    getTopicStatus,
    isWorkerReady,
    worker
  };
}