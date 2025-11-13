import React from 'react';

function FilterPanel({ filters, onFilterChange, availableAdders }) {
  const handleWatchStatusChange = (e) => {
    onFilterChange({ ...filters, watchStatus: e.target.value });
  };

  const handleAddedByChange = (e) => {
    onFilterChange({ ...filters, addedBy: e.target.value });
  };

  const handleClearFilters = () => {
    onFilterChange({ watchStatus: '', addedBy: '' });
  };

  const hasActiveFilters = filters.watchStatus || filters.addedBy;

  return (
    <div className="filter-panel">
      <div className="filter-group">
        <label className="filter-label">Watch Status:</label>
        <select 
          className="filter-select" 
          value={filters.watchStatus}
          onChange={handleWatchStatusChange}
        >
          <option value="">All</option>
          <option value="WATCHED">Watched</option>
          <option value="UNWATCHED">Unwatched</option>
        </select>
      </div>

      <div className="filter-group">
        <label className="filter-label">Added By:</label>
        <select 
          className="filter-select"
          value={filters.addedBy}
          onChange={handleAddedByChange}
        >
          <option value="">All</option>
          {availableAdders.map((adder, idx) => (
            <option key={idx} value={adder}>
              {adder}
            </option>
          ))}
        </select>
      </div>

      {hasActiveFilters && (
        <button 
          className="clear-filters-button"
          onClick={handleClearFilters}
          title="Clear all filters"
        >
          Clear Filters
        </button>
      )}
    </div>
  );
}

export default FilterPanel;
