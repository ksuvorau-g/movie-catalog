import React, { useState, useEffect } from 'react';
import axios from 'axios';
import CatalogList from './components/CatalogList';
import FilterPanel from './components/FilterPanel';
import AddMovieModal from './components/AddMovieModal';
import AddedByTabs from './components/AddedByTabs';
import RecommendationsBlock from './components/RecommendationsBlock';

const API_BASE_URL = '/api';

function App() {
  const [catalog, setCatalog] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filters, setFilters] = useState({
    watchStatus: '',
    addedBy: ''
  });
  const [availableAdders, setAvailableAdders] = useState([]);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [deletedIds, setDeletedIds] = useState(new Set());
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [notification, setNotification] = useState(null);

  // Load available adders once on mount
  useEffect(() => {
    const loadAvailableAdders = async () => {
      try {
        const response = await axios.get(`${API_BASE_URL}/catalog`);
        const adders = [...new Set(response.data
          .map(item => item.addedBy)
          .filter(adder => adder))];
        setAvailableAdders(adders.sort());
      } catch (err) {
        console.error('Error loading available adders:', err);
      }
    };
    
    loadAvailableAdders();
  }, []);

  useEffect(() => {
    fetchCatalog();
  }, [filters]);

  const fetchCatalog = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Build query parameters
      const params = {};
      if (filters.watchStatus) params.watchStatus = filters.watchStatus;
      if (filters.addedBy) params.addedBy = filters.addedBy;
      
      const response = await axios.get(`${API_BASE_URL}/catalog`, { params });
      setCatalog(response.data);
      
      // Clear deleted items on refresh
      setDeletedIds(new Set());
    } catch (err) {
      setError('Failed to load catalog. Make sure the backend is running on port 8080.');
      console.error('Error fetching catalog:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      fetchCatalog();
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response = await axios.get(`${API_BASE_URL}/catalog/search`, {
        params: { query: searchQuery }
      });
      setCatalog(response.data);
    } catch (err) {
      setError('Failed to search catalog.');
      console.error('Error searching catalog:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setSearchQuery('');
    setFilters({ watchStatus: '', addedBy: '' });
    fetchCatalog();
  };

  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
  };

  const handleAdderChange = (adder) => {
    setFilters({ ...filters, addedBy: adder });
  };

  const handleDelete = async (id) => {
    try {
      // Determine if it's a movie or series based on the catalog item
      const item = catalog.find(i => i.id === id);
      if (!item) return;

      const endpoint = item.contentType === 'MOVIE' 
        ? `${API_BASE_URL}/movies/${id}`
        : `${API_BASE_URL}/series/${id}`;

      await axios.delete(endpoint);
      
      // Mark item as deleted instead of removing it
      setDeletedIds(prev => new Set([...prev, id]));
    } catch (err) {
      setError('Failed to delete item.');
      console.error('Error deleting item:', err);
    }
  };

  const handleMarkAsWatched = async (id) => {
    try {
      // Determine if it's a movie or series based on the catalog item
      const item = catalog.find(i => i.id === id);
      if (!item) return;

      const endpoint = item.contentType === 'MOVIE' 
        ? `${API_BASE_URL}/movies/${id}/watch-status`
        : `${API_BASE_URL}/series/${id}/watch-status`;

      // Wait for the response to get the updated data
      const response = await axios.patch(endpoint, { watchStatus: 'WATCHED' });
      
      // Update the catalog with the full response data
      // For series, transform SeriesResponse to CatalogItemResponse format
      setCatalog(prevCatalog => 
        prevCatalog.map(catalogItem => {
          if (catalogItem.id === id) {
            return {
              ...catalogItem,
              ...response.data,
              contentType: catalogItem.contentType // Preserve contentType
            };
          }
          return catalogItem;
        })
      );
    } catch (err) {
      setError('Failed to update watch status.');
      console.error('Error updating watch status:', err);
    }
  };

  const handleMarkAsUnwatched = async (id) => {
    try {
      // Determine if it's a movie or series based on the catalog item
      const item = catalog.find(i => i.id === id);
      if (!item) return;

      const endpoint = item.contentType === 'MOVIE' 
        ? `${API_BASE_URL}/movies/${id}/watch-status`
        : `${API_BASE_URL}/series/${id}/watch-status`;

      // Wait for the response to get the updated data
      const response = await axios.patch(endpoint, { watchStatus: 'UNWATCHED' });
      
      // Update the catalog with the full response data
      // For series, transform SeriesResponse to CatalogItemResponse format
      setCatalog(prevCatalog => 
        prevCatalog.map(catalogItem => {
          if (catalogItem.id === id) {
            return {
              ...catalogItem,
              ...response.data,
              contentType: catalogItem.contentType // Preserve contentType
            };
          }
          return catalogItem;
        })
      );
    } catch (err) {
      setError('Failed to update watch status.');
      console.error('Error updating watch status:', err);
    }
  };

  const handleAddMovie = async (newItem) => {
    // Add the newly created item to the beginning of the catalog
    setCatalog(prevCatalog => [newItem, ...prevCatalog]);
    
    // Update available adders if the new item has an addedBy value
    if (newItem.addedBy && !availableAdders.includes(newItem.addedBy)) {
      setAvailableAdders(prev => [...prev, newItem.addedBy].sort());
    }
  };

  const showNotification = (message, type = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 5000);
  };

  const handleRefreshSeasons = async () => {
    if (isRefreshing) return;

    try {
      setIsRefreshing(true);
      setError(null);
      
      const response = await axios.post(`${API_BASE_URL}/series/refresh-all`);
      
      if (response.data.updatedCount > 0) {
        await fetchCatalog();
        showNotification(
          `Refresh completed! Processed: ${response.data.totalProcessed}, Updated: ${response.data.updatedCount}, Failed: ${response.data.failureCount}`,
          'success'
        );
      } else {
        showNotification(
          `Refresh completed! Processed: ${response.data.totalProcessed}. No updates found.`,
          'info'
        );
      }
    } catch (err) {
      console.error('Error refreshing seasons:', err);
      showNotification('Failed to refresh seasons. Please try again.', 'error');
    } finally {
      setIsRefreshing(false);
    }
  };

  return (
    <div className="app">
      {notification && (
        <div className={`notification notification-${notification.type}`}>
          {notification.message}
        </div>
      )}
      <header className="app-header">
        <h1>🎬 Movie Catalog</h1>
        <form className="search-form" onSubmit={handleSearch}>
          <input
            type="text"
            className="search-input"
            placeholder="Search movies and series..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <button type="submit" className="search-button">Search</button>
          {searchQuery && (
            <button type="button" className="reset-button" onClick={handleReset}>
              Clear
            </button>
          )}
        </form>
        <button 
          className="refresh-seasons-button"
          onClick={handleRefreshSeasons}
          disabled={isRefreshing}
        >
          {isRefreshing ? '🔄 Refreshing...' : '🔄 Refresh Seasons'}
        </button>
        <button 
          className="add-movie-button" 
          onClick={() => setIsAddModalOpen(true)}
        >
          ➕ Add Movie
        </button>
      </header>

      <AddedByTabs
        selectedAdder={filters.addedBy}
        onAdderChange={handleAdderChange}
        availableAdders={availableAdders}
      />

      <RecommendationsBlock addedBy={filters.addedBy} />

      <FilterPanel 
        filters={filters}
        onFilterChange={handleFilterChange}
      />

      <main className="app-main">
        {loading && <div className="loading">Loading catalog...</div>}
        {error && <div className="error">{error}</div>}
        {!loading && !error && (
          <CatalogList 
            items={catalog} 
            deletedIds={deletedIds}
            onDelete={handleDelete}
            onMarkAsWatched={handleMarkAsWatched}
            onMarkAsUnwatched={handleMarkAsUnwatched}
          />
        )}
      </main>

      <footer className="app-footer">
        <p>Total items: {catalog.length}</p>
      </footer>

      <AddMovieModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSave={handleAddMovie}
      />
    </div>
  );
}

export default App;
