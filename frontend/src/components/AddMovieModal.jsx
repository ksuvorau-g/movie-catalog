import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';

const API_BASE_URL = '/api';

const SEARCH_CONFIG = {
  minCharsForAutoSearch: 3,
  autoSearchDelayMs: 150,
  blurHideDelayMs: 50,
  maxResults: 5,
  imageBaseUrl: 'https://image.tmdb.org/t/p/w92'
};

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
  
  // Search state
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showSearchPopup, setShowSearchPopup] = useState(false);
  const searchTimerRef = useRef(null);
  const abortControllerRef = useRef(null);
  const blurTimerRef = useRef(null);
  const searchContainerRef = useRef(null);
  const titleInputRef = useRef(null);

  // Close search results when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (searchContainerRef.current && !searchContainerRef.current.contains(event.target)) {
        setShowSearchPopup(false);
      }
    };

    if (showSearchPopup) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [showSearchPopup]);

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

    // Handle title field for auto-search
    if (name === 'title') {
      // Clear existing timer
      if (searchTimerRef.current) {
        clearTimeout(searchTimerRef.current);
      }
      
      // Set new timer for auto-search
      if (value.length >= SEARCH_CONFIG.minCharsForAutoSearch) {
        searchTimerRef.current = setTimeout(() => {
          performSearch(value, false);
        }, SEARCH_CONFIG.autoSearchDelayMs);
      } else {
        // Clear results if below minimum
        setSearchResults([]);
        setShowSearchPopup(false);
      }
    }

    // Handle content type changes
    if (name === 'contentType') {
      // Clear search results when switching types
      setSearchResults([]);
      setShowSearchPopup(false);
      
      // Cancel any ongoing search
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
      if (searchTimerRef.current) {
        clearTimeout(searchTimerRef.current);
        searchTimerRef.current = null;
      }
      
      // Clear form data and image preview
      setFormData(prev => ({
        ...prev,
        contentType: value,
        title: '',
        coverImage: '',
        link: '',
        comment: '',
        genres: '',
        numberOfSeasons: ''
      }));
      setImagePreview(null);
      setImageId(null);
      setErrors({});
      
      return; // Don't continue with default form data update
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

  const performSearch = async (query, isManual = false) => {
    // Cancel any existing search
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    
    // Clear auto-search timer if manual search
    if (isManual && searchTimerRef.current) {
      clearTimeout(searchTimerRef.current);
      searchTimerRef.current = null;
    }
    
    // Validate query
    if (!query || query.trim().length === 0) {
      setSearchResults([]);
      setShowSearchPopup(false);
      return;
    }
    
    // Check minimum characters for auto-search
    if (!isManual && query.length < SEARCH_CONFIG.minCharsForAutoSearch) {
      return;
    }
    
    // Create new abort controller
    const controller = new AbortController();
    abortControllerRef.current = controller;
    
    setIsSearching(true);
    setShowSearchPopup(false);
    
    try {
      const endpoint = formData.contentType === 'MOVIE' 
        ? `${API_BASE_URL}/tmdb/search/movies`
        : `${API_BASE_URL}/tmdb/search/series`;
      
      const response = await axios.get(endpoint, {
        params: { title: query.trim() },
        signal: controller.signal
      });
      
      const results = response.data.slice(0, SEARCH_CONFIG.maxResults);
      setSearchResults(results);
      setShowSearchPopup(results.length > 0);
    } catch (error) {
      if (error.name === 'CanceledError' || error.code === 'ERR_CANCELED') {
        console.log('Search cancelled');
      } else {
        console.error('Search error:', error);
        setSearchResults([]);
      }
    } finally {
      setIsSearching(false);
      abortControllerRef.current = null;
    }
  };

  const handleManualSearch = (e) => {
    e.preventDefault();
    e.stopPropagation();
    performSearch(formData.title, true);
    // Refocus input after search so blur handler can work
    titleInputRef.current?.focus();
  };

  const handleTitleBlur = () => {
    // Delay hiding to allow clicking on results
    blurTimerRef.current = setTimeout(() => {
      setShowSearchPopup(false);
    }, SEARCH_CONFIG.blurHideDelayMs);
  };

  const handleTitleFocus = () => {
    // Clear blur timer if focus returns
    if (blurTimerRef.current) {
      clearTimeout(blurTimerRef.current);
      blurTimerRef.current = null;
    }
    
    // Don't automatically show popup on focus - only show after user interaction
  };

  const handleResultClick = async (result) => {
    // Close popup immediately
    setShowSearchPopup(false);
    
    try {
      setIsLoadingImage(true);
      
      // Call enrichment endpoint to get full metadata including genres and seasons
      const enrichmentEndpoint = formData.contentType === 'MOVIE'
        ? `${API_BASE_URL}/tmdb/enrich/movie`
        : `${API_BASE_URL}/tmdb/enrich/series`;
      
      const enrichmentResponse = await axios.post(enrichmentEndpoint, {
        tmdbId: result.id,
        downloadImage: true
      });
      
      const enrichedData = enrichmentResponse.data;
      
      // Build TMDB URL for description
      const tmdbUrl = formData.contentType === 'MOVIE'
        ? `https://www.themoviedb.org/movie/${result.id}`
        : `https://www.themoviedb.org/tv/${result.id}`;
      
      // Get genres from enrichment response
      const genres = enrichedData.genres ? enrichedData.genres.join(', ') : '';
      
      // Update form data with enriched metadata
      const updatedFormData = {
        title: enrichedData.title || result.original_title || result.original_name || result.title || result.name,
        link: tmdbUrl,
        genres: genres,
        coverImage: '', // Clear any previously entered URL
      };
      
      // Add number of seasons for series
      if (formData.contentType === 'SERIES' && enrichedData.totalSeasons) {
        updatedFormData.numberOfSeasons = enrichedData.totalSeasons.toString();
      }
      
      // Add cover image preview if downloaded, but leave coverImage field empty
      if (enrichedData.savedImageId) {
        setImageId(enrichedData.savedImageId);
        setImagePreview(`${API_BASE_URL}/images/${enrichedData.savedImageId}`);
        // Don't set coverImage in formData - leave it empty for user to see
      }
      
      setFormData(prev => ({
        ...prev,
        ...updatedFormData
      }));
      
    } catch (error) {
      console.error('Error enriching content:', error);
      
      // Fallback: use basic data from search result
      const originalTitle = result.original_title || result.original_name || result.title || result.name;
      const tmdbUrl = formData.contentType === 'MOVIE'
        ? `https://www.themoviedb.org/movie/${result.id}`
        : `https://www.themoviedb.org/tv/${result.id}`;
      
      setFormData(prev => ({
        ...prev,
        title: originalTitle,
        link: tmdbUrl,
      }));
      
      setErrors(prev => ({
        ...prev,
        general: 'Failed to fetch full metadata. Please fill in missing fields manually.'
      }));
    } finally {
      setIsLoadingImage(false);
    }
  };

  const renderSearchResults = () => {
    if (!showSearchPopup) return null;
    
    if (isSearching) {
      return (
        <div className="search-results-popup">
          <div className="search-loading">Searching...</div>
        </div>
      );
    }
    
    if (searchResults.length === 0) {
      return null;
    }
    
    return (
      <div className="search-results-popup">
        {searchResults.map((result) => {
          const title = result.original_title || result.original_name || 'Unknown Title';
          const year = result.release_date 
            ? new Date(result.release_date).getFullYear()
            : result.first_air_date 
              ? new Date(result.first_air_date).getFullYear()
              : '';
          // Backend returns full URL in poster_path (snake_case from TMDB API)
          const posterUrl = result.poster_path || null;
          
          return (
            <div
              key={result.id}
              className="search-result-item"
              onMouseDown={(e) => e.preventDefault()} // Prevent blur on click
              onClick={() => handleResultClick(result)}
            >
              {posterUrl ? (
                <img
                  src={posterUrl}
                  alt={title}
                  className="search-result-poster"
                />
              ) : (
                <div className="search-result-poster-placeholder">
                  🎬
                </div>
              )}
              <div className="search-result-info">
                <div className="search-result-title">{title}</div>
                {year && <div className="search-result-year">{year}</div>}
              </div>
            </div>
          );
        })}
      </div>
    );
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
      let response;
      if (formData.contentType === 'MOVIE') {
        response = await axios.post(`${API_BASE_URL}/movies`, baseData);
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
        
        response = await axios.post(`${API_BASE_URL}/series`, seriesData);
      }
      
      // Transform the response to catalog item format
      const newItem = {
        ...response.data,
        contentType: formData.contentType,
        watchStatus: response.data.seriesWatchStatus || response.data.watchStatus
      };
      
      handleClose();
      onSave(newItem); // Pass the newly created item to parent
    } catch (error) {
      console.error('Error saving:', error);
      setErrors({ submit: `Failed to save ${formData.contentType.toLowerCase()}. Please try again.` });
    }
  };

  const handleClose = () => {
    // Clear timers
    if (searchTimerRef.current) {
      clearTimeout(searchTimerRef.current);
      searchTimerRef.current = null;
    }
    if (blurTimerRef.current) {
      clearTimeout(blurTimerRef.current);
      blurTimerRef.current = null;
    }
    
    // Cancel any ongoing search
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      abortControllerRef.current = null;
    }
    
    // Clear search state
    setSearchResults([]);
    setIsSearching(false);
    setShowSearchPopup(false);
    
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
            <div className="search-container" ref={searchContainerRef}>
              <div className="title-input-wrapper">
                <input
                  type="text"
                  id="title"
                  name="title"
                  ref={titleInputRef}
                  className={`form-input ${errors.title ? 'error' : ''}`}
                  value={formData.title}
                  onChange={handleChange}
                  onBlur={handleTitleBlur}
                  onFocus={handleTitleFocus}
                  placeholder={`Search for ${formData.contentType === 'MOVIE' ? 'movie' : 'series'} title`}
                  autoFocus
                  style={{ flex: 1 }}
                />
                <button
                  type="button"
                  className="search-button-inline"
                  onClick={handleManualSearch}
                  disabled={!formData.title || formData.title.trim().length === 0}
                >
                  Search
                </button>
              </div>
              {errors.title && <span className="error-message">{errors.title}</span>}
              {renderSearchResults()}
            </div>
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
