import React, { useState } from 'react';
import axios from 'axios';

const API_BASE_URL = '/api';

function AddMovieModal({ isOpen, onClose, onSave }) {
  const [formData, setFormData] = useState({
    contentType: 'MOVIE',
    title: '',
    coverImage: '',
    link: '',
    comment: '',
    addedBy: '',
    genres: '',
    numberOfSeasons: ''
  });
  
  const [errors, setErrors] = useState({});
  const [imagePreview, setImagePreview] = useState(null);
  const [imageId, setImageId] = useState(null);
  const [isLoadingImage, setIsLoadingImage] = useState(false);

  const handleChange = async (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }

    // If coverImage URL is changed, download and preview the image
    if (name === 'coverImage' && value.trim()) {
      await downloadImage(value.trim());
    } else if (name === 'coverImage' && !value.trim()) {
      // Clear image preview if URL is cleared
      setImagePreview(null);
      setImageId(null);
    }
  };

  const downloadImage = async (imageUrl) => {
    // Basic URL validation
    try {
      new URL(imageUrl);
    } catch {
      return; // Invalid URL, skip download
    }

    setIsLoadingImage(true);
    setErrors(prev => ({ ...prev, coverImage: '' }));

    try {
      const response = await axios.post(`${API_BASE_URL}/images/download`, {
        imageUrl: imageUrl
      });

      if (response.data) {
        setImageId(response.data.id);
        setImagePreview(`${API_BASE_URL}/images/${response.data.id}`);
      }
    } catch (error) {
      console.error('Error downloading image:', error);
      setErrors(prev => ({
        ...prev,
        coverImage: 'Failed to download image. Please check the URL.'
      }));
      setImagePreview(null);
      setImageId(null);
    } finally {
      setIsLoadingImage(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
    }
    
    if (formData.contentType === 'SERIES') {
      if (!formData.numberOfSeasons || formData.numberOfSeasons <= 0) {
        newErrors.numberOfSeasons = 'Number of seasons must be at least 1';
      }
    }
    
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const newErrors = validateForm();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    // Parse genres from comma-separated string to array
    const genres = formData.genres
      .split(',')
      .map(g => g.trim())
      .filter(g => g);

    const baseData = {
      title: formData.title.trim(),
      coverImage: imageId || null, // Use image ID instead of URL
      link: formData.link.trim() || null,
      comment: formData.comment.trim() || null,
      addedBy: formData.addedBy.trim() || null,
      genres: genres.length > 0 ? genres : null,
      priority: 0 // default priority
    };

    try {
      if (formData.contentType === 'MOVIE') {
        await axios.post(`${API_BASE_URL}/movies`, baseData);
      } else {
        // For series, create seasons array with all unwatched
        const seasons = [];
        const numSeasons = parseInt(formData.numberOfSeasons);
        for (let i = 1; i <= numSeasons; i++) {
          seasons.push({
            seasonNumber: i,
            watchStatus: 'UNWATCHED'
          });
        }
        
        const seriesData = {
          ...baseData,
          seasons: seasons,
          seriesStatus: 'ONGOING' // default status
        };
        
        await axios.post(`${API_BASE_URL}/series`, seriesData);
      }
      
      handleClose();
      onSave(); // Notify parent to refresh the catalog
    } catch (error) {
      console.error('Error saving:', error);
      setErrors({ submit: `Failed to save ${formData.contentType.toLowerCase()}. Please try again.` });
    }
  };

  const handleClose = () => {
    setFormData({
      contentType: 'MOVIE',
      title: '',
      coverImage: '',
      link: '',
      comment: '',
      addedBy: '',
      genres: '',
      numberOfSeasons: ''
    });
    setErrors({});
    setImagePreview(null);
    setImageId(null);
    setIsLoadingImage(false);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>Add New {formData.contentType === 'MOVIE' ? 'Movie' : 'Series'}</h2>
          <button className="modal-close-button" onClick={handleClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label htmlFor="contentType" className="form-label">
              Content Type <span className="required">*</span>
            </label>
            <div className="content-type-selector">
              <label className="radio-label">
                <input
                  type="radio"
                  name="contentType"
                  value="MOVIE"
                  checked={formData.contentType === 'MOVIE'}
                  onChange={handleChange}
                />
                <span>Movie</span>
              </label>
              <label className="radio-label">
                <input
                  type="radio"
                  name="contentType"
                  value="SERIES"
                  checked={formData.contentType === 'SERIES'}
                  onChange={handleChange}
                />
                <span>Series</span>
              </label>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="title" className="form-label">
              Title <span className="required">*</span>
            </label>
            <input
              type="text"
              id="title"
              name="title"
              className={`form-input ${errors.title ? 'error' : ''}`}
              value={formData.title}
              onChange={handleChange}
              placeholder="Enter movie title"
              autoFocus
            />
            {errors.title && <span className="error-message">{errors.title}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="coverImage" className="form-label">
              Cover Image URL
            </label>
            <input
              type="url"
              id="coverImage"
              name="coverImage"
              className={`form-input ${errors.coverImage ? 'error' : ''}`}
              value={formData.coverImage}
              onChange={handleChange}
              placeholder="https://example.com/image.jpg"
              disabled={isLoadingImage}
            />
            {errors.coverImage && <span className="error-message">{errors.coverImage}</span>}
            {isLoadingImage && (
              <div className="image-loading">
                <span>Downloading image...</span>
              </div>
            )}
            {imagePreview && !isLoadingImage && (
              <div className="image-preview">
                <img src={imagePreview} alt="Cover preview" />
                <span className="image-preview-label">✓ Image loaded successfully</span>
              </div>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="link" className="form-label">
              {formData.contentType === 'MOVIE' ? 'Movie' : 'Series'} Description URL
            </label>
            <input
              type="url"
              id="link"
              name="link"
              className="form-input"
              value={formData.link}
              onChange={handleChange}
              placeholder={`https://example.com/${formData.contentType === 'MOVIE' ? 'movie' : 'series'}-page`}
            />
          </div>

          {formData.contentType === 'SERIES' && (
            <div className="form-group">
              <label htmlFor="numberOfSeasons" className="form-label">
                Number of Seasons <span className="required">*</span>
              </label>
              <input
                type="number"
                id="numberOfSeasons"
                name="numberOfSeasons"
                className={`form-input ${errors.numberOfSeasons ? 'error' : ''}`}
                value={formData.numberOfSeasons}
                onChange={handleChange}
                placeholder="e.g., 3"
                min="1"
              />
              {errors.numberOfSeasons && <span className="error-message">{errors.numberOfSeasons}</span>}
              <small className="form-help-text">All seasons will be set to UNWATCHED by default</small>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="comment" className="form-label">
              Comment
            </label>
            <textarea
              id="comment"
              name="comment"
              className="form-textarea"
              value={formData.comment}
              onChange={handleChange}
              placeholder={`Add your thoughts about this ${formData.contentType === 'MOVIE' ? 'movie' : 'series'}...`}
              rows="3"
            />
          </div>

          <div className="form-group">
            <label htmlFor="addedBy" className="form-label">
              Added By
            </label>
            <input
              type="text"
              id="addedBy"
              name="addedBy"
              className="form-input"
              value={formData.addedBy}
              onChange={handleChange}
              placeholder="Your name"
            />
          </div>

          <div className="form-group">
            <label htmlFor="genres" className="form-label">
              Genres
            </label>
            <input
              type="text"
              id="genres"
              name="genres"
              className="form-input"
              value={formData.genres}
              onChange={handleChange}
              placeholder="Action, Drama, Comedy (comma-separated)"
            />
            <small className="form-help-text">Separate multiple genres with commas</small>
          </div>

          <div className="form-info">
            {formData.contentType === 'MOVIE' ? (
              <p>Watch Status will be set to: <strong>UNWATCHED</strong></p>
            ) : (
              <p>All seasons will be set to: <strong>UNWATCHED</strong></p>
            )}
            <p>Priority will be set to: <strong>0 (default)</strong></p>
          </div>

          {errors.submit && <div className="error-message submit-error">{errors.submit}</div>}

          <div className="modal-actions">
            <button type="button" className="button button-cancel" onClick={handleClose}>
              Cancel
            </button>
            <button type="submit" className="button button-save">
              Save {formData.contentType === 'MOVIE' ? 'Movie' : 'Series'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddMovieModal;
