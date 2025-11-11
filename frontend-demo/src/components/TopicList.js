// Topic List - 대화 목록을 표시하는 컴포넌트
import React from 'react';

export default function TopicList({ 
  topics, 
  currentTopicId, 
  activeTopics, 
  connectionStatus,
  onSelectTopic, 
  onCreateTopic, 
  onDeleteTopic 
}) {
  
  const getStatusIcon = (topicId) => {
    const status = connectionStatus[topicId];
    const isActive = activeTopics.includes(topicId);
    
    switch(status) {
      case 'connecting':
        return '🔄';
      case 'connected':
        return '🟢';
      case 'reconnecting':
        return '🟡';
      case 'error':
        return '🔴';
      case 'completed':
        return '✅';
      case 'closed':
      case 'stopped':
        return '⚫';
      default:
        return isActive ? '🟢' : '⚪';
    }
  };
  
  const getStatusText = (topicId) => {
    const status = connectionStatus[topicId];
    const isActive = activeTopics.includes(topicId);
    
    if (isActive) {
      switch(status) {
        case 'connecting': return '연결 중...';
        case 'connected': return '응답 대기';
        case 'reconnecting': return '재연결 중...';
        case 'error': return '에러 발생';
        case 'completed': return '응답 완료';
        default: return '활성';
      }
    }
    
    return status === 'completed' ? '완료됨' : '대기';
  };

  return (
    <div className="topic-list">
      <div className="topic-list-header">
        <h2>대화 목록</h2>
        <button 
          className="create-topic-btn"
          onClick={onCreateTopic}
          title="새 대화 시작"
        >
          + 새 대화
        </button>
      </div>
      
      <div className="topic-items">
        {topics.map(topic => {
          const isSelected = topic.id === currentTopicId;
          const isActive = activeTopics.includes(topic.id);
          
          return (
            <div 
              key={topic.id}
              className={`topic-item ${isSelected ? 'selected' : ''} ${isActive ? 'active' : ''}`}
              onClick={() => onSelectTopic(topic.id)}
            >
              <div className="topic-header">
                <div className="topic-status">
                  <span className="status-icon">
                    {getStatusIcon(topic.id)}
                  </span>
                  <span className="topic-name">
                    {topic.name}
                  </span>
                </div>
                
                <button 
                  className="delete-btn"
                  onClick={(e) => {
                    e.stopPropagation();
                    onDeleteTopic(topic.id);
                  }}
                  title="대화 삭제"
                >
                  ×
                </button>
              </div>
              
              <div className="topic-info">
                <div className="topic-id">
                  ID: {topic.id.slice(-8)}
                </div>
                <div className="topic-status-text">
                  {getStatusText(topic.id)}
                </div>
                <div className="topic-time">
                  {new Date(topic.createdAt).toLocaleTimeString()}
                </div>
              </div>
              
              {isActive && (
                <div className="activity-indicator">
                  <div className="pulse"></div>
                  백그라운드 실행 중
                </div>
              )}
            </div>
          );
        })}
      </div>
      
      {topics.length === 0 && (
        <div className="empty-topics">
          <p>아직 대화가 없습니다.</p>
          <button onClick={onCreateTopic}>
            첫 번째 대화 시작하기
          </button>
        </div>
      )}
      
      <div className="topic-list-footer">
        <div className="summary">
          총 {topics.length}개 대화
          {activeTopics.length > 0 && (
            <span className="active-summary">
              ({activeTopics.length}개 활성)
            </span>
          )}
        </div>
      </div>
    </div>
  );
}