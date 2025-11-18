import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE_URL = '/api';

const RecommendationsBlock = ({ addedBy }) => {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchRecommendations();
  }, [addedBy]);

  const fetchRecommendations = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const params = { count: 4 };
      if (addedBy) {
        params.addedBy = addedBy;
      }
      
      const response = await axios.get(`${API_BASE_URL}/recommendations`, { params });
      setRecommendations(response.data);
    } catch (err) {
      setError('Failed to load recommendations');
      console.error('Error fetching recommendations:', err);
    } finally {
      setLoading(false);
    }
  };

  const getPriorityEmoji = (priority) => {
    if (!priority || priority === 0) return '';
    return '⭐'.repeat(Math.min(priority, 5));
  };

  const getCoverImageUrl = (coverImage) => {
    if (!coverImage) return null;
    if (coverImage.startsWith('http://') || coverImage.startsWith('https://')) {
      return coverImage;
    }
    return `${API_BASE_URL}/images/${coverImage}`;
  };

  if (loading) {
    return (
      <div className="recommendations-block">
        <div className="recommendations-header">
          <span className="recommendations-icon">🎯</span>
          Recommendations for You
        </div>
        <div className="recommendations-loading">
          <span>⏳</span>
          Loading recommendations...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="recommendations-block">
        <div className="recommendations-header">
          <span className="recommendations-icon">🎯</span>
          Recommendations for You
        </div>
        <div className="recommendations-error">{error}</div>
      </div>
    );
  }

  return (
    <div className="recommendations-block">
      <div className="recommendations-header">
        <span className="recommendations-icon">🎯</span>
        Recommendations for {addedBy || 'Everyone'}
      </div>
      
      {recommendations.length === 0 ? (
        <div className="recommendations-empty">
          No recommendations available. All content watched! 🎉
        </div>
      ) : (
        <div className="recommendations-list">
          {recommendations.map((item) => {
            const coverImageUrl = getCoverImageUrl(item.coverImage);
            return (
              <a
                key={item.id}
                href={item.link}
                target="_blank"
                rel="noopener noreferrer"
                className="recommendation-card"
                title={item.comment || item.title}
              >
                {coverImageUrl && (
                  <img
                    src={coverImageUrl}
                    alt={item.title}
                    className="recommendation-image"
                    onError={(e) => {
                      e.target.style.display = 'none';
                    }}
                  />
                )}
                <div className="recommendation-type">
                  {item.contentType === 'MOVIE' ? '🎬 Movie' : '📺 Series'}
                  {item.hasNewSeasons && ' 🆕'}
                </div>
                <div className="recommendation-title">{item.title}</div>
                {item.priority > 0 && (
                  <div className="recommendation-priority">
                    {getPriorityEmoji(item.priority)}
                  </div>
                )}
              </a>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default RecommendationsBlock;
