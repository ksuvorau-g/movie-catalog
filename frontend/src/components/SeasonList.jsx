import React from 'react';
import axios from 'axios';

const API_BASE_URL = '/api';

function SeasonList({ seriesId, seasons, onSeasonUpdate, tmdbLink }) {
  const [updating, setUpdating] = React.useState(null);
  const [managingSeasons, setManagingSeasons] = React.useState(false);
  const [syncingSeasons, setSyncingSeasons] = React.useState(false);

  const hasTmdbLink = React.useMemo(() => {
    if (!tmdbLink) {
      return false;
    }
    const normalized = tmdbLink.toLowerCase();
    return normalized.includes('themoviedb.org/tv') || normalized.includes('tmdb.org/tv');
  }, [tmdbLink]);

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

  const handleAddSeason = async () => {
    setManagingSeasons(true);
    try {
      const response = await axios.post(
        `${API_BASE_URL}/series/${seriesId}/seasons/increase`
      );

      if (onSeasonUpdate) {
        onSeasonUpdate(response.data);
      }
    } catch (error) {
      console.error('Failed to add season:', error);
      const message = error.response?.data?.message || 'Failed to add season. Please try again.';
      alert(message);
    } finally {
      setManagingSeasons(false);
    }
  };

  const handleRemoveSeason = async () => {
    setManagingSeasons(true);
    try {
      const response = await axios.post(
        `${API_BASE_URL}/series/${seriesId}/seasons/decrease`
      );

      if (onSeasonUpdate) {
        onSeasonUpdate(response.data);
      }
    } catch (error) {
      console.error('Failed to remove season:', error);
      const message = error.response?.data?.message || 'Failed to remove season. Please try again.';
      alert(message);
    } finally {
      setManagingSeasons(false);
    }
  };

  const handleFetchSeasons = async () => {
    setSyncingSeasons(true);
    try {
      const response = await axios.post(
        `${API_BASE_URL}/series/${seriesId}/refresh`
      );

      if (onSeasonUpdate) {
        onSeasonUpdate(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch seasons from TMDB:', error);
      const message = error.response?.data?.message || 'Failed to fetch seasons from TMDB. Please try again.';
      alert(message);
    } finally {
      setSyncingSeasons(false);
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
          <div className="season-manage-buttons">
            <button
              className="season-manage-button add"
              onClick={handleAddSeason}
              disabled={managingSeasons}
              title="Add season"
            >
              +
            </button>
            <button
              className="season-manage-button remove"
              onClick={handleRemoveSeason}
              disabled={managingSeasons || totalCount <= 1}
              title="Remove last season"
            >
              −
            </button>
          </div>
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

      {hasTmdbLink && (
        <div className="season-sync-panel">
          <button
            className="season-sync-button"
            onClick={handleFetchSeasons}
            disabled={syncingSeasons}
            title="Sync seasons with TMDB"
          >
            {syncingSeasons ? 'Fetching…' : 'Fetch Seasons'}
          </button>
        </div>
      )}
    </div>
  );
}

export default SeasonList;
