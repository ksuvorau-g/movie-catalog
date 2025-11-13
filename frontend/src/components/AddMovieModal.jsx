import React, { useState } from 'react';
import axios from 'axios';

const API_BASE_URL = '/api';

function AddMovieModal({ isOpen, onClose, onSave }) {
  const [formData, setFormData] = useState({
    title: '',
    coverImage: '',
    link: '',
    comment: '',
    addedBy: '',
    genres: ''
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

    const movieData = {
      title: formData.title.trim(),
      coverImage: imageId || null, // Use image ID instead of URL
      link: formData.link.trim() || null,
      comment: formData.comment.trim() || null,
      addedBy: formData.addedBy.trim() || null,
      genres: genres.length > 0 ? genres : null,
      priority: 0 // default priority
    };

    try {
      await onSave(movieData);
      handleClose();
    } catch (error) {
      setErrors({ submit: 'Failed to save movie. Please try again.' });
    }
  };

  const handleClose = () => {
    setFormData({
      title: '',
      coverImage: '',
      link: '',
      comment: '',
      addedBy: '',
      genres: ''
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
          <h2>Add New Movie</h2>
          <button className="modal-close-button" onClick={handleClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
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
              Movie Description URL
            </label>
            <input
              type="url"
              id="link"
              name="link"
              className="form-input"
              value={formData.link}
              onChange={handleChange}
              placeholder="https://example.com/movie-page"
            />
          </div>

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
              placeholder="Add your thoughts about this movie..."
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
            <p>Watch Status will be set to: <strong>UNWATCHED</strong></p>
            <p>Priority will be set to: <strong>0 (default)</strong></p>
          </div>

          {errors.submit && <div className="error-message submit-error">{errors.submit}</div>}

          <div className="modal-actions">
            <button type="button" className="button button-cancel" onClick={handleClose}>
              Cancel
            </button>
            <button type="submit" className="button button-save">
              Save Movie
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddMovieModal;
