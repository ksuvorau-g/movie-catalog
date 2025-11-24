import React, { useState } from 'react';
import axios from 'axios';
import SeasonList from './SeasonList';
import EditModal from './EditModal';

const API_BASE_URL = '/api';

function CatalogList({ items, deletedIds = new Set(), onDelete, onMarkAsWatched, onMarkAsUnwatched, onNotificationsRefresh }) {
  const [localItems, setLocalItems] = useState(items);
  const [expandedSeries, setExpandedSeries] = useState(new Set());
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [itemToEdit, setItemToEdit] = useState(null);

  React.useEffect(() => {
    setLocalItems(items);
  }, [items]);

  if (!localItems || localItems.length === 0) {
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

  const handlePriorityChange = async (id, contentType, currentPriority, delta) => {
    const newPriority = Math.max(0, currentPriority + delta);
    
    try {
      const endpoint = contentType === 'MOVIE' 
        ? `${API_BASE_URL}/movies/${id}/priority`
        : `${API_BASE_URL}/series/${id}/priority`;
      
      await axios.patch(endpoint, { priority: newPriority });
      
      // Update local state without full refresh
      setLocalItems(prevItems => 
        prevItems.map(item => 
          item.id === id ? { ...item, priority: newPriority } : item
        )
      );
    } catch (error) {
      console.error('Failed to update priority:', error);
    }
  };

  const toggleSeasonExpand = (seriesId) => {
    setExpandedSeries(prevExpanded => {
      const newExpanded = new Set(prevExpanded);
      if (newExpanded.has(seriesId)) {
        newExpanded.delete(seriesId);
      } else {
        newExpanded.add(seriesId);
      }
      return newExpanded;
    });
  };

  const handleSeasonUpdate = (seriesId, updatedSeriesData) => {
    // Update the local state with the new series data from the API
    setLocalItems(prevItems =>
      prevItems.map(item => {
        if (item.id === seriesId) {
          return {
            ...item,
            ...updatedSeriesData,
            contentType: item.contentType // Preserve contentType from original item
          };
        }
        return item;
      })
    );
  };

  const handleEdit = (item) => {
    setItemToEdit(item);
    setIsEditModalOpen(true);
  };

  const handleCloseEdit = () => {
    setIsEditModalOpen(false);
    setItemToEdit(null);
  };

  const handleEditSave = (updatedItem) => {
    // Update the local state with the updated item
    setLocalItems(prevItems =>
      prevItems.map(item =>
        item.id === updatedItem.id ? updatedItem : item
      )
    );
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
      {localItems.map((item) => {
        const isDeleted = deletedIds.has(item.id);
        return (
        <div key={item.id} className={`catalog-item ${isDeleted ? 'deleted' : ''}`}>
          <div className="catalog-item-actions">
            <button 
              className="action-button edit-button"
              onClick={() => handleEdit(item)}
              title="Edit item"
              disabled={isDeleted}
            >
              ✏️ Edit
            </button>
            <button 
              className="action-button remove-button"
              onClick={() => handleDelete(item.id, item.title)}
              title={isDeleted ? "Removed" : "Remove from catalog"}
              disabled={isDeleted}
            >
              {isDeleted ? 'Removed' : 'Remove'}
            </button>
            {item.watchStatus === 'WATCHED' ? (
              <button 
                className="action-button unwatched-button"
                onClick={() => handleMarkAsUnwatched(item.id)}
                title="Mark as unwatched"
                disabled={isDeleted}
              >
                Mark as Unwatched
              </button>
            ) : (
              <button 
                className="action-button watched-button"
                onClick={() => handleMarkAsWatched(item.id)}
                title="Mark as watched"
                disabled={isDeleted}
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
                {item.hasNewSeasons && item.watchStatus === 'UNWATCHED' && (
                  <div className="new-season-badge">New Season Available!</div>
                )}
                {item.seasons && item.seasons.length > 0 && (
                  <div className="season-management">
                    <button
                      className="season-expand-button"
                      onClick={() => toggleSeasonExpand(item.id)}
                      disabled={isDeleted}
                    >
                      {expandedSeries.has(item.id) ? (
                        <>
                          <span className="expand-icon">▼</span>
                          Hide Seasons
                        </>
                      ) : (
                        <>
                          <span className="expand-icon">▶</span>
                          Manage Seasons
                        </>
                      )}
                    </button>
                    {expandedSeries.has(item.id) && !isDeleted && (
                      <SeasonList
                        seriesId={item.id}
                        seasons={item.seasons}
                        tmdbLink={item.link}
                        onSeasonUpdate={(updatedData) => handleSeasonUpdate(item.id, updatedData)}
                        onNotificationsRefresh={onNotificationsRefresh}
                      />
                    )}
                  </div>
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
              <div className="detail-row priority-row">
                <span className="detail-label">Priority:</span>
                <div className="priority-controls">
                  {item.priority > 0 && (
                    <button 
                      className="priority-button priority-decrease"
                      onClick={() => handlePriorityChange(item.id, item.contentType, item.priority, -1)}
                      title="Decrease priority"
                      disabled={isDeleted}
                    >
                      −
                    </button>
                  )}
                  <span className="detail-value priority">
                    {item.priority === 0 ? '0' : '⭐'.repeat(item.priority)}
                  </span>
                  <button 
                    className="priority-button priority-increase"
                    onClick={() => handlePriorityChange(item.id, item.contentType, item.priority, 1)}
                    title="Increase priority"
                    disabled={isDeleted}
                  >
                    +
                  </button>
                </div>
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
      );
      })}
      
      <EditModal
        isOpen={isEditModalOpen}
        onClose={handleCloseEdit}
        onSave={handleEditSave}
        item={itemToEdit}
      />
    </div>
  );
}

export default CatalogList;
