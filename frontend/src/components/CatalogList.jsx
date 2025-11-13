import React from 'react';

const API_BASE_URL = '/api';

function CatalogList({ items, onDelete, onMarkAsWatched, onMarkAsUnwatched }) {
  if (!items || items.length === 0) {
    return <div className="empty-state">No items in catalog</div>;
  }

  const handleDelete = async (id, title) => {
    if (window.confirm(`Are you sure you want to remove "${title}"?`)) {
      await onDelete(id);
    }
  };

  const handleMarkAsWatched = async (id) => {
    await onMarkAsWatched(id);
  };

  const handleMarkAsUnwatched = async (id) => {
    await onMarkAsUnwatched(id);
  };

  // Helper function to determine if coverImage is a URL or an image ID
  const getCoverImageUrl = (coverImage) => {
    if (!coverImage) return null;
    
    // Check if it's a URL (starts with http:// or https://)
    if (coverImage.startsWith('http://') || coverImage.startsWith('https://')) {
      return coverImage;
    }
    
    // Otherwise, treat it as an image ID and construct the API URL
    return `${API_BASE_URL}/images/${coverImage}`;
  };

  return (
    <div className="catalog-list">
      {items.map((item) => (
        <div key={item.id} className="catalog-item">
          <div className="catalog-item-actions">
            <button 
              className="action-button remove-button"
              onClick={() => handleDelete(item.id, item.title)}
              title="Remove from catalog"
            >
              Remove
            </button>
            {item.watchStatus === 'WATCHED' ? (
              <button 
                className="action-button unwatched-button"
                onClick={() => handleMarkAsUnwatched(item.id)}
                title="Mark as unwatched"
              >
                Mark as Unwatched
              </button>
            ) : (
              <button 
                className="action-button watched-button"
                onClick={() => handleMarkAsWatched(item.id)}
                title="Mark as watched"
              >
                Mark as Watched
              </button>
            )}
          </div>

          <div className="catalog-item-header">
            <h3 className="catalog-item-title">{item.title}</h3>
            <span className={`content-type-badge ${item.contentType?.toLowerCase()}`}>
              {item.contentType}
            </span>
          </div>

          {item.coverImage && (
            <div className="catalog-item-image-container">
              <img 
                src={getCoverImageUrl(item.coverImage)} 
                alt={item.title}
                className="catalog-item-image"
                onError={(e) => {
                  e.target.parentElement.style.display = 'none';
                }}
              />
              {item.comment && (
                <div className="catalog-item-comment-overlay">
                  <div className="comment-content">{item.comment}</div>
                </div>
              )}
            </div>
          )}

          <div className="catalog-item-details">
            {item.genres && item.genres.length > 0 && (
              <div className="detail-row">
                <span className="detail-label">Genres:</span>
                <span className="detail-value genres">
                  {item.genres.map((genre, idx) => (
                    <span key={idx} className="genre-tag">{genre}</span>
                  ))}
                </span>
              </div>
            )}

            {item.watchStatus && (
              <div className="detail-row">
                <span className="detail-label">Status:</span>
                <span className={`detail-value watch-status ${item.watchStatus.toLowerCase()}`}>
                  {item.watchStatus}
                </span>
              </div>
            )}

            {item.contentType === 'MOVIE' && item.length && (
              <div className="detail-row">
                <span className="detail-label">Length:</span>
                <span className="detail-value">{item.length} min</span>
              </div>
            )}

            {item.contentType === 'SERIES' && (
              <>
                {item.seriesStatus && (
                  <div className="detail-row">
                    <span className="detail-label">Series Status:</span>
                    <span className="detail-value">{item.seriesStatus}</span>
                  </div>
                )}
                {item.totalAvailableSeasons && (
                  <div className="detail-row">
                    <span className="detail-label">Seasons:</span>
                    <span className="detail-value">{item.totalAvailableSeasons}</span>
                  </div>
                )}
                {item.hasNewSeasons && (
                  <div className="new-season-badge">New Season Available!</div>
                )}
              </>
            )}

            {item.addedBy && (
              <div className="detail-row">
                <span className="detail-label">Added by:</span>
                <span className="detail-value">{item.addedBy}</span>
              </div>
            )}

            {item.priority !== null && item.priority !== undefined && (
              <div className="detail-row">
                <span className="detail-label">Priority:</span>
                <span className="detail-value priority">
                  {'⭐'.repeat(item.priority)}
                </span>
              </div>
            )}

            {item.dateAdded && (
              <div className="detail-row">
                <span className="detail-label">Added:</span>
                <span className="detail-value">
                  {new Date(item.dateAdded).toISOString().split('T')[0]}
                </span>
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

export default CatalogList;
