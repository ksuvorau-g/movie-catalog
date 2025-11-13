# Frontend Development Setup

## Hot Reload Development Mode

For faster development with instant hot reload (no Docker rebuild needed):

### Quick Start

1. **Start backend services:**
   ```bash
   make dev-backend
   ```
   This starts MongoDB and the Spring Boot API in Docker containers.

2. **Start frontend dev server (in a new terminal or same one):**
   ```bash
   make serve
   ```
   Or directly:
   ```bash
   cd frontend
   npm start
   ```

The dev server will:
- ✅ Run on http://localhost:3000
- ✅ Auto-reload on file changes (hot module replacement)
- ✅ Proxy API requests to http://localhost:8080
- ✅ Show compilation errors in the browser

### Making Changes

Just edit any file in `frontend/src/` and save - the changes will appear instantly in your browser!

### Switching Back to Docker

When you want to run everything in Docker:

```bash
# Stop the dev server (Ctrl+C in the terminal, or)
make stop-serve

# Start full Docker stack
make run
```

### Troubleshooting

**Port 3000 already in use:**
```bash
# Stop Docker frontend container
docker compose stop frontend

# Or stop all
make stop
```

**Module not found errors:**
```bash
cd frontend
npm install
```

**API requests failing:**
Make sure backend is running: `make dev-backend`

## Project Structure

```
frontend/
├── src/
│   ├── App.jsx              # Main app component
│   ├── index.jsx            # Entry point
│   ├── styles.css           # Global styles
│   └── components/
│       ├── CatalogList.jsx  # Movie/series list
│       └── FilterPanel.jsx  # Filter controls
├── public/
│   └── index.html           # HTML template
├── webpack.config.js        # Webpack configuration
└── package.json             # Dependencies
```

## Available Scripts

- `npm start` - Start dev server with hot reload
- `npm run build` - Production build
- `npm install` - Install dependencies
