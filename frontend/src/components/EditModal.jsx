import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE_URL = '/api';

function EditModal({ isOpen, onClose, onSave, item }) {
  const [formData, setFormData] = useState({
    title: '',
    coverImage: '',
    comment: '',
    genres: ''
  });
  
  const [errors, setErrors] = useState({});
  const [imagePreview, setImagePreview] = useState(null);
  const [imageId, setImageId] = useState(null);
  const [isLoadingImage, setIsLoadingImage] = useState(false);

  // Initialize form data when item prop changes
  useEffect(() => {
    if (item && isOpen) {
      setFormData({
        title: item.title || '',
        coverImage: '', // Leave empty - will show preview from existing image
        comment: item.comment || '',
        genres: item.genres ? item.genres.join(', ') : ''
      });

      // Set image preview from existing cover image
      if (item.coverImage) {
        const imageUrl = item.coverImage.startsWith('http://') || item.coverImage.startsWith('https://')
          ? item.coverImage
          : `${API_BASE_URL}/images/${item.coverImage}`;
        setImagePreview(imageUrl);
        // Store the image ID if it's not a URL
        if (!item.coverImage.startsWith('http')) {
          setImageId(item.coverImage);
        }
      } else {
        setImagePreview(null);
        setImageId(null);
      }

      setErrors({});
      setIsLoadingImage(false);
    }
  }, [item, isOpen]);

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
      // Clear image preview if URL is cleared - restore original
      if (item.coverImage) {
        const imageUrl = item.coverImage.startsWith('http://') || item.coverImage.startsWith('https://')
          ? item.coverImage
          : `${API_BASE_URL}/images/${item.coverImage}`;
        setImagePreview(imageUrl);
        if (!item.coverImage.startsWith('http')) {
          setImageId(item.coverImage);
        }
      } else {
        setImagePreview(null);
        setImageId(null);
      }
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
      // Restore original image preview on error
      if (item.coverImage) {
        const imageUrl = item.coverImage.startsWith('http://') || item.coverImage.startsWith('https://')
          ? item.coverImage
          : `${API_BASE_URL}/images/${item.coverImage}`;
        setImagePreview(imageUrl);
      } else {
        setImagePreview(null);
        setImageId(null);
      }
    } finally {
      setIsLoadingImage(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
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

    const updateData = {
      title: formData.title.trim(),
      coverImage: imageId || item.coverImage || null,
      comment: formData.comment.trim() || null,
      genres: genres.length > 0 ? genres : null
    };

    // For series, preserve the existing seasons array and link
    if (item.contentType === 'SERIES') {
      updateData.seasons = item.seasons || [];
      updateData.link = item.link || null;
    }

    try {
      const endpoint = item.contentType === 'MOVIE' 
        ? `${API_BASE_URL}/movies/${item.id}`
        : `${API_BASE_URL}/series/${item.id}`;

      const response = await axios.put(endpoint, updateData);
      
      // Transform the response to include contentType
      const updatedItem = {
        ...response.data,
        contentType: item.contentType
      };
      
      handleClose();
      onSave(updatedItem); // Pass the updated item to parent
    } catch (error) {
      console.error('Error updating:', error);
      setErrors({ submit: `Failed to update ${item.contentType?.toLowerCase() || 'item'}. Please try again.` });
    }
  };

  const handleClose = () => {
    setFormData({
      title: '',
      coverImage: '',
      comment: '',
      genres: ''
    });
    setErrors({});
    setImagePreview(null);
    setImageId(null);
    setIsLoadingImage(false);
    onClose();
  };

  if (!isOpen || !item) return null;

  // Determine display values for read-only fields
  const descriptionUrl = item.link || null;
  const numberOfSeasons = item.contentType === 'SERIES' && item.totalAvailableSeasons
    ? item.totalAvailableSeasons
    : item.contentType === 'SERIES' && item.seasons
      ? item.seasons.length
      : null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>Edit {item.contentType === 'MOVIE' ? 'Movie' : 'Series'}</h2>
          <button className="modal-close-button" onClick={handleClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label className="form-label">Content Type</label>
            <div className="read-only-field">
              <span className={`content-type-badge ${item.contentType?.toLowerCase()}`}>
                {item.contentType}
              </span>
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
              placeholder="Enter title"
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
                <span className="image-preview-label">
                  {formData.coverImage ? '✓ New image loaded' : '✓ Current image'}
                </span>
              </div>
            )}
          </div>

          {descriptionUrl && (
            <div className="form-group">
              <label className="form-label">
                {item.contentType === 'MOVIE' ? 'Movie' : 'Series'} Description URL
              </label>
              <div className="read-only-field">
                <a 
                  href={descriptionUrl} 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="description-link"
                >
                  {descriptionUrl}
                </a>
              </div>
              <small className="form-help-text">Read-only field</small>
            </div>
          )}

          {numberOfSeasons !== null && (
            <div className="form-group">
              <label className="form-label">Number of Seasons</label>
              <div className="read-only-field">
                <span className="season-count-label">{numberOfSeasons} season{numberOfSeasons !== 1 ? 's' : ''}</span>
              </div>
              <small className="form-help-text">Use "Manage Seasons" in catalog to modify seasons</small>
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
              placeholder={`Add your thoughts about this ${item.contentType === 'MOVIE' ? 'movie' : 'series'}...`}
              rows="3"
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

          {errors.submit && <div className="error-message submit-error">{errors.submit}</div>}

          <div className="modal-actions">
            <button type="button" className="button button-cancel" onClick={handleClose}>
              Cancel
            </button>
            <button type="submit" className="button button-save">
              Save Changes
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditModal;
