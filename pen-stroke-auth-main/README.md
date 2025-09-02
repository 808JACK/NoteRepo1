# NoteIt - Frontend

## Project Overview

A modern React-based frontend for the NoteIt note-taking application with JWT authentication.

## Features

- 🔐 JWT Authentication with automatic token refresh
- 📝 Create, edit, delete, and search notes
- 👤 User registration with OTP verification
- 🎨 Modern UI with Tailwind CSS
- 🔒 Secure token management
- 📱 Responsive design

## Tech Stack

- React 18 with TypeScript
- Tailwind CSS for styling
- Vite for build tooling
- JWT authentication

## Getting Started

### Prerequisites

- Node.js & npm installed - [install with nvm](https://github.com/nvm-sh/nvm#installing-and-updating)

Follow these steps:

```sh
# Step 1: Clone the repository using the project's Git URL.
git clone <YOUR_GIT_URL>

# Step 2: Navigate to the project directory.
cd <YOUR_PROJECT_NAME>

# Step 3: Install the necessary dependencies.
npm i

# Step 4: Start the development server with auto-reloading and an instant preview.
npm run dev
```

**Edit a file directly in GitHub**

- Navigate to the desired file(s).
- Click the "Edit" button (pencil icon) at the top right of the file view.
- Make your changes and commit the changes.

**Use GitHub Codespaces**

- Navigate to the main page of your repository.
- Click on the "Code" button (green button) near the top right.
- Select the "Codespaces" tab.
- Click on "New codespace" to launch a new Codespace environment.
- Edit files directly within the Codespace and commit and push your changes once you're done.

## What technologies are used for this project?

This project is built with:

- Vite
- TypeScript
- React
- shadcn-ui
- Tailwind CSS

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

### Build

```bash
npm run build
```

## Environment Variables

Create a `.env` file in the root directory:

```
VITE_API_BASE_URL=https://noteit-backend-ixqz.onrender.com
```

## Deployment

### Vercel Deployment

1. Connect your GitHub repository to Vercel
2. Set environment variables in Vercel dashboard
3. Deploy automatically on push to main branch

### Manual Deployment

```bash
npm run build
# Deploy the dist folder to your hosting service
```

## Project Structure

```
src/
├── components/     # Reusable UI components
├── contexts/       # React contexts (Auth, etc.)
├── lib/           # Utility functions and API calls
├── pages/         # Page components
└── main.tsx       # Application entry point
```
