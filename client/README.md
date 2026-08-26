# MiniJira - Frontend Client

This is the frontend client for the MiniJira application, built with React, TypeScript, Vite, Tailwind CSS, React Router, and Axios.

## Prerequisites

Ensure you have the following installed on your system:
- [Node.js](https://nodejs.org/) (v18 or higher recommended)
- [npm](https://www.npmjs.com/) (usually comes with Node.js)

## Getting Started

Follow these steps to run the client application locally:

### 1. Navigate to the client directory
From the root of the project, run:
```bash
cd client
```

### 2. Install dependencies
Install all the required npm packages:
```bash
npm install
```

### 3. Run the development server
Start the local Vite development server:
```bash
npm run dev
```

Once started, the CLI will output the local address (typically `http://localhost:5173`). Open this URL in your web browser to view the application.

---

## Available Scripts

In the `client` directory, you can run the following commands:

*   **`npm run dev`**: Starts the development server with Hot Module Replacement (HMR).
*   **`npm run build`**: Compiles and builds the production-ready assets into the `dist` folder.
*   **`npm run lint`**: Runs [Oxlint](https://oxc.rs/docs/guide/usage/linter/index.html) to quickly analyze the codebase for potential errors and styling issues.
*   **`npm run preview`**: Previews the production build locally (run `npm run build` first).

---

## Backend Integration

The client is configured to communicate with the Spring Boot backend API at:
*   **Base URL**: `http://localhost:8080` (defined in `src/api.ts`)

Please make sure the Spring Boot backend server is running on port `8080` for the frontend features (such as user authentication, project management, and tasks) to work properly.
