import React, { useState, useEffect } from 'react';
import axios from 'axios';
import CatalogList from './components/CatalogList';
import FilterPanel from './components/FilterPanel';
import AddMovieModal from './components/AddMovieModal';

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
      
      // Extract unique adders from catalog
      const adders = [...new Set(response.data
        .map(item => item.addedBy)
        .filter(adder => adder))];
      setAvailableAdders(adders.sort());
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

  const handleDelete = async (id) => {
    try {
      // Determine if it's a movie or series based on the catalog item
      const item = catalog.find(i => i.id === id);
      if (!item) return;

      const endpoint = item.contentType === 'MOVIE' 
        ? `${API_BASE_URL}/movies/${id}`
        : `${API_BASE_URL}/series/${id}`;

      await axios.delete(endpoint);
      
      // Refresh the catalog after deletion
      await fetchCatalog();
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

      // Optimistically update the local state
      setCatalog(prevCatalog => 
        prevCatalog.map(catalogItem => 
          catalogItem.id === id 
            ? { ...catalogItem, watchStatus: 'WATCHED' }
            : catalogItem
        )
      );

      const endpoint = item.contentType === 'MOVIE' 
        ? `${API_BASE_URL}/movies/${id}/watch-status`
        : `${API_BASE_URL}/series/${id}/watch-status`;

      // Send the request in the background
      axios.patch(endpoint, { watchStatus: 'WATCHED' }).catch(err => {
        // Revert the optimistic update on error
        setError('Failed to update watch status.');
        console.error('Error updating watch status:', err);
        fetchCatalog(); // Reload to get correct state
      });
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

      // Optimistically update the local state
      setCatalog(prevCatalog => 
        prevCatalog.map(catalogItem => 
          catalogItem.id === id 
            ? { ...catalogItem, watchStatus: 'UNWATCHED' }
            : catalogItem
        )
      );

      const endpoint = item.contentType === 'MOVIE' 
        ? `${API_BASE_URL}/movies/${id}/watch-status`
        : `${API_BASE_URL}/series/${id}/watch-status`;

      // Send the request in the background
      axios.patch(endpoint, { watchStatus: 'UNWATCHED' }).catch(err => {
        // Revert the optimistic update on error
        setError('Failed to update watch status.');
        console.error('Error updating watch status:', err);
        fetchCatalog(); // Reload to get correct state
      });
    } catch (err) {
      setError('Failed to update watch status.');
      console.error('Error updating watch status:', err);
    }
  };

  const handleAddMovie = () => {
    // Refresh the catalog after adding a movie/series
    fetchCatalog();
  };

  return (
    <div className="app">
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
          className="add-movie-button" 
          onClick={() => setIsAddModalOpen(true)}
        >
          ➕ Add Movie
        </button>
      </header>

      <FilterPanel 
        filters={filters}
        onFilterChange={handleFilterChange}
        availableAdders={availableAdders}
      />

      <main className="app-main">
        {loading && <div className="loading">Loading catalog...</div>}
        {error && <div className="error">{error}</div>}
        {!loading && !error && (
          <CatalogList 
            items={catalog} 
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
