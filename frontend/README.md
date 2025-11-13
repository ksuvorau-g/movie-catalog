# Movie Catalog Frontend

A simple React-based frontend for the Movie Catalog application.

## Features

- Display catalog of movies and series in a grid layout
- Search functionality to find specific titles
- Responsive design that works on desktop and mobile
- Shows detailed information including:
  - Content type (Movie/Series)
  - Genres
  - Watch status
  - Priority ratings
  - Series-specific info (seasons, new season alerts)

## Development Setup

### Prerequisites

- Node.js 18+ and npm
- Backend API running on http://localhost:8080

### Quick Start with Hot Reload (Recommended)

For the best development experience with instant updates:

```bash
# From project root - starts backend services (MongoDB + API)
make dev-backend

# Then start frontend dev server with hot reload
make serve
```

Or manually:
```bash
cd frontend
npm install
npm start
```

The application will open at http://localhost:3000 with **hot module replacement** enabled.
Any changes to files in `src/` will instantly appear in your browser without manual refresh!

See [DEV_SETUP.md](./DEV_SETUP.md) for detailed development workflow.

### Install Dependencies

```bash
cd frontend
npm install
```

### Run Development Server

```bash
npm start
```

The application will open in your browser at http://localhost:3000

### Build for Production

```bash
npm run build
```

This creates optimized files in the `dist/` directory.

## Docker Setup

### Build and Run with Docker Compose

From the project root directory:

```bash
docker-compose up --build
```

This will start:
- MongoDB on port 27017
- Backend API on port 8080
- Frontend on port 3000

Access the application at http://localhost:3000

### Run Frontend Container Only

```bash
cd frontend
docker build -t moviecat-frontend .
docker run -p 3000:80 moviecat-frontend
```

## Project Structure

```
frontend/
├── public/
│   └── index.html          # HTML template
├── src/
│   ├── components/
│   │   └── CatalogList.jsx # Catalog display component
│   ├── App.jsx             # Main application component
│   ├── index.jsx           # Application entry point
│   └── styles.css          # Global styles
├── Dockerfile              # Docker configuration
├── nginx.conf              # Nginx configuration for production
├── package.json            # Dependencies and scripts
└── webpack.config.js       # Webpack configuration
```

## API Integration

The frontend connects to the backend API at:
- Development: `http://localhost:8080/api`
- Production (Docker): `/api` (proxied through nginx)

### Endpoints Used

- `GET /api/catalog` - Fetch all catalog items
- `GET /api/catalog/search?query={query}` - Search catalog

## Customization

### Change API URL

Edit `src/App.jsx` and update the `API_BASE_URL` constant:

```javascript
const API_BASE_URL = 'http://your-api-url:port/api';
```

### Styling

Modify `src/styles.css` to customize colors, fonts, and layout.

## Technologies

- React 18
- Axios for HTTP requests
- Webpack for bundling
- Babel for transpilation
- Nginx for production serving
