// Chat Manager - 여러 topic을 관리하는 메인 컴포넌트
import React, { useState, useEffect } from 'react';
import { useSSEWorker } from '../hooks/useSSEWorker.js';
import ChatRoom from './ChatRoom.js';
import TopicList from './TopicList.js';

export default function ChatManager() {
  const { 
    startSSE, 
    stopSSE, 
    activeTopics, 
    connectionStatus, 
    getTopicStatus,
    isWorkerReady 
  } = useSSEWorker();
  
  const [currentTopicId, setCurrentTopicId] = useState(null);
  const [topics, setTopics] = useState([]); // 생성된 모든 topic 목록
  const [chatHistory, setChatHistory] = useState({}); // topicId -> messages[]
  
  // 새로운 topic 생성
  const createNewTopic = () => {
    const newTopicId = `topic_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    setTopics(prev => [...prev, {
      id: newTopicId,
      name: `대화 ${topics.length + 1}`,
      createdAt: new Date().toISOString(),
      status: 'created'
    }]);
    
    setChatHistory(prev => ({
      ...prev,
      [newTopicId]: []
    }));
    
    setCurrentTopicId(newTopicId);
    
    return newTopicId;
  };
  
  // topic 선택
  const selectTopic = (topicId) => {
    setCurrentTopicId(topicId);
  };
  
  // 메시지 전송
  const sendMessage = async (topicId, content) => {
    if (!isWorkerReady) {
      alert('Worker가 준비되지 않았습니다.');
      return;
    }
    
    // 사용자 메시지 추가
    setChatHistory(prev => ({
      ...prev,
      [topicId]: [
        ...(prev[topicId] || []),
        {
          id: Date.now(),
          role: 'user',
          content,
          timestamp: new Date().toISOString(),
          completed: true
        }
      ]
    }));
    
    // AI 응답 메시지 초기화
    const aiMessageId = Date.now() + 1;
    setChatHistory(prev => ({
      ...prev,
      [topicId]: [
        ...prev[topicId],
        {
          id: aiMessageId,
          role: 'assistant',
          content: '',
          timestamp: new Date().toISOString(),
          completed: false
        }
      ]
    }));
    
    // 스트리밍 메시지 핸들러
    const handleStreamMessage = (data) => {
      console.log(`[${topicId}] 스트리밍 데이터 수신:`, data);
      
      // Chat 객체의 message.content에서 내용 추출
      const messageContent = data?.message?.content || data?.content || data?.text || '';
      
      console.log(`[${topicId}] 추출된 메시지 내용:`, messageContent);
      console.log(`[${topicId}] AI 메시지 ID:`, aiMessageId);
      
      if (!messageContent) {
        console.warn(`[${topicId}] 메시지 내용이 비어있음:`, data);
        return;
      }
      
      setChatHistory(prev => {
        const updatedHistory = {
          ...prev,
          [topicId]: prev[topicId].map(msg => 
            msg.id === aiMessageId 
              ? { ...msg, content: msg.content + messageContent }
              : msg
          )
        };
        
        console.log(`[${topicId}] 업데이트된 채팅 히스토리:`, updatedHistory[topicId]);
        return updatedHistory;
      });
    };
    
    // 스트림 완료 핸들러
    const handleStreamComplete = () => {
      setChatHistory(prev => ({
        ...prev,
        [topicId]: prev[topicId].map(msg => 
          msg.id === aiMessageId 
            ? { ...msg, completed: true }
            : msg
        )
      }));
    };
    
    // 에러 핸들러
    const handleError = (error) => {
      setChatHistory(prev => ({
        ...prev,
        [topicId]: prev[topicId].map(msg => 
          msg.id === aiMessageId 
            ? { ...msg, content: `에러 발생: ${error}`, completed: true, error: true }
            : msg
        )
      }));
    };
    
    // SSE 연결 시작
    const success = startSSE(topicId, handleStreamMessage, handleError, handleStreamComplete);
    
    if (success) {
      // 실제 서버에 메시지 전송 시뮬레이션
      // 실제로는 fetch('/v1/chat', { method: 'POST', ... })
      simulateServerRequest(topicId, content);
    }
  };
  
  // 실제 서버에 메시지 전송
  const simulateServerRequest = async (topicId, content) => {
    console.log(`서버에 메시지 전송: ${topicId}`, content);
    
    try {
      const response = await fetch('http://localhost:8080/v1/chat', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream'
        },
        body: JSON.stringify({
          topicUlid: topicId,
          content: content
        })
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      console.log('메시지 전송 성공:', response.status);
    } catch (error) {
      console.error('메시지 전송 실패:', error);
      
      // 에러 메시지를 채팅에 표시
      setChatHistory(prev => ({
        ...prev,
        [topicId]: [
          ...prev[topicId],
          {
            id: Date.now(),
            role: 'error',
            content: `전송 실패: ${error.message}`,
            timestamp: new Date().toISOString(),
            completed: true,
            error: true
          }
        ]
      }));
    }
  };
  
  // topic 삭제
  const deleteTopic = (topicId) => {
    // SSE 연결 중지
    stopSSE(topicId);
    
    // topic 목록에서 제거
    setTopics(prev => prev.filter(topic => topic.id !== topicId));
    
    // 채팅 히스토리 제거
    setChatHistory(prev => {
      const newHistory = { ...prev };
      delete newHistory[topicId];
      return newHistory;
    });
    
    // 현재 선택된 topic이면 다른 topic으로 변경
    if (currentTopicId === topicId) {
      const remainingTopics = topics.filter(topic => topic.id !== topicId);
      setCurrentTopicId(remainingTopics.length > 0 ? remainingTopics[0].id : null);
    }
  };
  
  // 초기 topic 생성
  useEffect(() => {
    if (topics.length === 0) {
      createNewTopic();
    }
  }, []);
  
  return (
    <div className="chat-manager">
      <div className="chat-header">
        <h1>Multi-Topic Chat with Background SSE</h1>
        <div className="worker-status">
          Worker 상태: {isWorkerReady ? '✅ 준비됨' : '❌ 준비 중...'}
          {activeTopics.length > 0 && (
            <span className="active-count">
              | 활성 대화: {activeTopics.length}개
            </span>
          )}
        </div>
      </div>
      
      <div className="chat-layout">
        {/* 왼쪽: Topic 목록 */}
        <div className="topic-sidebar">
          <TopicList
            topics={topics}
            currentTopicId={currentTopicId}
            activeTopics={activeTopics}
            connectionStatus={connectionStatus}
            onSelectTopic={selectTopic}
            onCreateTopic={createNewTopic}
            onDeleteTopic={deleteTopic}
          />
        </div>
        
        {/* 오른쪽: 현재 선택된 채팅방 */}
        <div className="chat-main">
          {currentTopicId ? (
            <ChatRoom
              topicId={currentTopicId}
              messages={chatHistory[currentTopicId] || []}
              onSendMessage={(content) => sendMessage(currentTopicId, content)}
              connectionStatus={getTopicStatus(currentTopicId)}
              isActive={activeTopics.includes(currentTopicId)}
            />
          ) : (
            <div className="no-topic">
              <p>대화를 시작하려면 새 Topic을 생성하세요.</p>
              <button onClick={createNewTopic}>새 대화 시작</button>
            </div>
          )}
        </div>
      </div>
      
      {/* 하단: 백그라운드 활동 표시 */}
      {activeTopics.length > 1 && (
        <div className="background-activity">
          <h3>백그라운드 대화 진행 중:</h3>
          <div className="background-topics">
            {activeTopics
              .filter(topicId => topicId !== currentTopicId)
              .map(topicId => {
                const topic = topics.find(t => t.id === topicId);
                const status = getTopicStatus(topicId);
                return (
                  <div 
                    key={topicId} 
                    className={`background-topic status-${status}`}
                    onClick={() => selectTopic(topicId)}
                  >
                    <span>{topic?.name || topicId}</span>
                    <span className="status-indicator">{status}</span>
                  </div>
                );
              })
            }
          </div>
        </div>
      )}
    </div>
  );
}