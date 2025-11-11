// Chat Room - 개별 대화방 컴포넌트
import React, { useState, useRef, useEffect } from 'react';

export default function ChatRoom({ 
  topicId, 
  messages, 
  onSendMessage, 
  connectionStatus,
  isActive 
}) {
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);
  
  // 메시지가 추가될 때 스크롤을 맨 아래로
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);
  
  // 컴포넌트 마운트 시 입력창에 포커스
  useEffect(() => {
    inputRef.current?.focus();
  }, [topicId]);
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const content = inputValue.trim();
    if (!content) return;
    
    setIsLoading(true);
    setInputValue('');
    
    try {
      await onSendMessage(content);
    } catch (error) {
      console.error('메시지 전송 실패:', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  const getConnectionStatusText = () => {
    switch(connectionStatus) {
      case 'connecting': return '연결 중...';
      case 'connected': return '연결됨';
      case 'reconnecting': return '재연결 중...';
      case 'error': return '연결 오류';
      case 'completed': return '응답 완료';
      case 'closed':
      case 'stopped': return '연결 종료';
      default: return '대기 중';
    }
  };
  
  const getConnectionStatusColor = () => {
    switch(connectionStatus) {
      case 'connected': return '#4CAF50';
      case 'connecting':
      case 'reconnecting': return '#FF9800';
      case 'error': return '#F44336';
      case 'completed': return '#2196F3';
      default: return '#9E9E9E';
    }
  };
  
  const formatTimestamp = (timestamp) => {
    return new Date(timestamp).toLocaleTimeString();
  };
  
  const getExampleMessages = () => {
    return [
      "안녕하세요! 어떻게 도와드릴까요?",
      "오늘 날씨는 어떤가요?", 
      "JavaScript에 대해 설명해주세요",
      "재미있는 농담 하나 해주세요",
      "React Hook의 장점은 무엇인가요?"
    ];
  };

  return (
    <div className="chat-room">
      {/* 채팅방 헤더 */}
      <div className="chat-room-header">
        <div className="topic-info">
          <h2>대화: {topicId.slice(-8)}</h2>
          <div className="connection-status">
            <span 
              className="status-dot"
              style={{ backgroundColor: getConnectionStatusColor() }}
            ></span>
            {getConnectionStatusText()}
            {isActive && <span className="active-badge">실행 중</span>}
          </div>
        </div>
        
        <div className="chat-stats">
          <span>메시지: {messages.length}</span>
        </div>
      </div>
      
      {/* 메시지 영역 */}
      <div className="messages-container">
        {messages.length === 0 && (
          <div className="welcome-message">
            <h3>새로운 대화를 시작하세요!</h3>
            <p>아래 예시 메시지를 클릭하거나 직접 입력해보세요:</p>
            <div className="example-messages">
              {getExampleMessages().map((example, index) => (
                <button
                  key={index}
                  className="example-message"
                  onClick={() => setInputValue(example)}
                  disabled={isLoading}
                >
                  {example}
                </button>
              ))}
            </div>
          </div>
        )}
        
        {messages.map(message => (
          <div 
            key={message.id} 
            className={`message ${message.role} ${message.error ? 'error' : ''}`}
          >
            <div className="message-header">
              <span className="role-badge">
                {message.role === 'user' ? '👤 사용자' : '🤖 AI'}
              </span>
              <span className="timestamp">
                {formatTimestamp(message.timestamp)}
              </span>
              {!message.completed && (
                <span className="streaming-indicator">
                  <span className="dots">...</span>
                </span>
              )}
            </div>
            
            <div className="message-content">
              {message.content}
            </div>
            
            {message.error && (
              <div className="error-info">
                ⚠️ 메시지 전송 중 오류가 발생했습니다.
              </div>
            )}
          </div>
        ))}
        
        <div ref={messagesEndRef} />
      </div>
      
      {/* 입력 영역 */}
      <form className="message-input-form" onSubmit={handleSubmit}>
        <div className="input-container">
          <input
            ref={inputRef}
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder={
              isLoading 
                ? "메시지 전송 중..." 
                : connectionStatus === 'error'
                ? "연결 오류 - 재시도하세요"
                : "메시지를 입력하세요..."
            }
            disabled={isLoading}
            className="message-input"
          />
          
          <button 
            type="submit" 
            disabled={isLoading || !inputValue.trim()}
            className="send-button"
          >
            {isLoading ? (
              <span className="loading-spinner">⏳</span>
            ) : (
              '전송'
            )}
          </button>
        </div>
        
        <div className="input-hints">
          <span className="hint">
            Enter를 눌러 전송 • 
            {isActive ? ' 백그라운드에서 응답 수신 중' : ' 대기 상태'}
          </span>
        </div>
      </form>
      
      {/* 백그라운드 알림 */}
      {isActive && connectionStatus === 'connected' && (
        <div className="background-notification">
          🔄 이 대화는 백그라운드에서 실행됩니다. 
          다른 페이지로 이동해도 응답 완료 시 알림을 받을 수 있습니다.
        </div>
      )}
    </div>
  );
}