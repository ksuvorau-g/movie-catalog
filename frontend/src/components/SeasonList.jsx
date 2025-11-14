import React from 'react';
import axios from 'axios';

const API_BASE_URL = '/api';

function SeasonList({ seriesId, seasons, onSeasonUpdate }) {
  const [updating, setUpdating] = React.useState(null);

  const handleSeasonToggle = async (seasonNumber, currentStatus) => {
    const newStatus = currentStatus === 'WATCHED' ? 'UNWATCHED' : 'WATCHED';
    setUpdating(seasonNumber);

    try {
      const response = await axios.patch(
        `${API_BASE_URL}/series/${seriesId}/seasons/${seasonNumber}/watch-status`,
        { watchStatus: newStatus }
      );
      
      // Update parent component with the full updated series data
      if (onSeasonUpdate) {
        onSeasonUpdate(response.data);
      }
    } catch (error) {
      console.error('Failed to update season watch status:', error);
      alert('Failed to update season status. Please try again.');
    } finally {
      setUpdating(null);
    }
  };

  const handleMarkAllWatched = async () => {
    try {
      const response = await axios.patch(
        `${API_BASE_URL}/series/${seriesId}/watch-status`,
        { watchStatus: 'WATCHED' }
      );
      
      if (onSeasonUpdate) {
        onSeasonUpdate(response.data);
      }
    } catch (error) {
      console.error('Failed to mark all seasons as watched:', error);
      alert('Failed to update all seasons. Please try again.');
    }
  };

  const handleMarkAllUnwatched = async () => {
    try {
      const response = await axios.patch(
        `${API_BASE_URL}/series/${seriesId}/watch-status`,
        { watchStatus: 'UNWATCHED' }
      );
      
      if (onSeasonUpdate) {
        onSeasonUpdate(response.data);
      }
    } catch (error) {
      console.error('Failed to mark all seasons as unwatched:', error);
      alert('Failed to update all seasons. Please try again.');
    }
  };

  if (!seasons || seasons.length === 0) {
    return (
      <div className="season-list-empty">
        No seasons available for this series.
      </div>
    );
  }

  const watchedCount = seasons.filter(s => s.watchStatus === 'WATCHED').length;
  const totalCount = seasons.length;

  return (
    <div className="season-list">
      <div className="season-list-header">
        <div className="season-progress">
          <span className="season-progress-text">
            {watchedCount} of {totalCount} seasons watched
          </span>
          <div className="season-progress-bar">
            <div 
              className="season-progress-fill"
              style={{ width: `${(watchedCount / totalCount) * 100}%` }}
            />
          </div>
        </div>
        <div className="season-bulk-actions">
          <button 
            className="season-bulk-button"
            onClick={handleMarkAllWatched}
            disabled={watchedCount === totalCount}
          >
            Mark All Watched
          </button>
          <button 
            className="season-bulk-button"
            onClick={handleMarkAllUnwatched}
            disabled={watchedCount === 0}
          >
            Mark All Unwatched
          </button>
        </div>
      </div>

      <div className="season-items">
        {seasons.map((season) => (
          <div 
            key={season.seasonNumber} 
            className={`season-item ${season.watchStatus.toLowerCase()}`}
          >
            <div className="season-info">
              <span className="season-number">Season {season.seasonNumber}</span>
            </div>
            <button
              className={`season-toggle-button ${season.watchStatus.toLowerCase()}`}
              onClick={() => handleSeasonToggle(season.seasonNumber, season.watchStatus)}
              disabled={updating === season.seasonNumber}
            >
              {updating === season.seasonNumber ? (
                <span className="season-loading">⏳</span>
              ) : season.watchStatus === 'WATCHED' ? (
                <>
                  <span className="season-icon">✓</span>
                  <span>Watched</span>
                </>
              ) : (
                <>
                  <span className="season-icon">○</span>
                  <span>Unwatched</span>
                </>
              )}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default SeasonList;
