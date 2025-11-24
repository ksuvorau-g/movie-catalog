import React, { useState } from 'react';
import axios from 'axios';

const API_BASE_URL = '/api';

function NotificationPanel({ notifications, onNotificationDismissed }) {
  const [isOpen, setIsOpen] = useState(false);
  const [dismissingIds, setDismissingIds] = useState(new Set());

  const handleDismiss = async (notificationId) => {
    if (dismissingIds.has(notificationId)) return;

    try {
      setDismissingIds(prev => new Set([...prev, notificationId]));
      await axios.delete(`${API_BASE_URL}/notifications/${notificationId}`);
      
      // Notify parent component to refresh notifications
      if (onNotificationDismissed) {
        onNotificationDismissed();
      }
    } catch (error) {
      console.error('Failed to dismiss notification:', error);
      alert('Failed to dismiss notification. Please try again.');
    } finally {
      setDismissingIds(prev => {
        const updated = new Set(prev);
        updated.delete(notificationId);
        return updated;
      });
    }
  };

  const togglePanel = () => {
    setIsOpen(!isOpen);
  };

  const notificationCount = notifications.length;

  return (
    <div className="notification-panel-container">
      <button 
        className="notification-bell-button" 
        onClick={togglePanel}
        title={`${notificationCount} new notification${notificationCount !== 1 ? 's' : ''}`}
        aria-label="Notifications"
      >
        <span className="notification-bell-icon">🔔</span>
        {notificationCount > 0 && (
          <span className="notification-badge">{notificationCount}</span>
        )}
      </button>

      {isOpen && (
        <>
          <div className="notification-panel-overlay" onClick={togglePanel}></div>
          <div className="notification-panel">
            <div className="notification-panel-header">
              <h3>Notifications</h3>
              <button className="notification-panel-close" onClick={togglePanel}>
                ✕
              </button>
            </div>

            <div className="notification-panel-content">
              {notifications.length === 0 ? (
                <div className="notification-panel-empty">
                  No new notifications
                </div>
              ) : (
                <ul className="notification-list">
                  {notifications.map(notification => (
                    <li 
                      key={notification.id} 
                      className={`notification-item ${dismissingIds.has(notification.id) ? 'dismissing' : ''}`}
                    >
                      <div className="notification-item-content">
                        <div className="notification-item-icon">🎬</div>
                        <div className="notification-item-details">
                          <div className="notification-item-title">
                            {notification.seriesTitle}
                          </div>
                          <div className="notification-item-message">
                            {notification.message}
                          </div>
                          <div className="notification-item-meta">
                            {notification.newSeasonsCount} new season{notification.newSeasonsCount > 1 ? 's' : ''} available
                          </div>
                        </div>
                      </div>
                      <button
                        className="notification-item-dismiss"
                        onClick={() => handleDismiss(notification.id)}
                        disabled={dismissingIds.has(notification.id)}
                        title="Dismiss notification"
                      >
                        {dismissingIds.has(notification.id) ? '...' : '✕'}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}

export default NotificationPanel;
