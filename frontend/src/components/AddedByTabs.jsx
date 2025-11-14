import React from 'react';

function AddedByTabs({ selectedAdder, onAdderChange, availableAdders }) {
  return (
    <div className="added-by-tabs">
      <button
        className={`tab ${!selectedAdder ? 'active' : ''}`}
        onClick={() => onAdderChange('')}
      >
        All
      </button>
      {availableAdders.map((adder) => (
        <button
          key={adder}
          className={`tab ${selectedAdder === adder ? 'active' : ''}`}
          onClick={() => onAdderChange(adder)}
        >
          {adder}
        </button>
      ))}
    </div>
  );
}

export default AddedByTabs;
