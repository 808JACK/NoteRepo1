# NoteIt - Full Stack Note Taking App

A modern note-taking application built with Spring Boot backend and React frontend.

## Features

- **User Authentication**: Email-based signup with OTP verification
- **Secure Login**: JWT-based authentication with refresh tokens
- **CRUD Operations**: Create, read, update, and delete notes
- **Search Functionality**: Search through your notes by title or content
- **Responsive Design**: Beautiful UI built with React and Tailwind CSS
- **Real-time Updates**: Seamless note management with instant feedback

## Tech Stack

### Backend (NoteIt)

- **Spring Boot 3.5.0** - Main framework
- **PostgreSQL** - User data storage
- **MongoDB** - Notes storage
- **Spring Security** - Authentication and authorization
- **JWT** - Token-based authentication
- **Spring Mail** - Email service for OTP
- **Maven** - Dependency management

### Frontend (pen-stroke-auth-main)

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **Shadcn/ui** - UI components
- **React Router** - Navigation
- **React Query** - State management
- **Vite** - Build tool

## Getting Started

### Prerequisites

- Java 21+
- Node.js 18+
- PostgreSQL database
- MongoDB database

### Backend Setup

1. **Navigate to the backend directory:**

   ```bash
   cd NoteIt
   ```

2. **Configure the database:**

   - Update `src/main/resources/application.properties` with your database credentials
   - The app uses PostgreSQL for user data and MongoDB for notes

3. **Run the backend:**

   ```bash
   ./mvnw spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

### Frontend Setup

1. **Navigate to the frontend directory:**

   ```bash
   cd pen-stroke-auth-main
   ```

2. **Set up environment variables:**

   ```bash
   cp .env.example .env
   ```

   Update `.env` with your backend URL:

   ```
   VITE_API_BASE_URL=http://localhost:8085
   ```

3. **Install dependencies:**

   ```bash
   npm install
   ```

4. **Start the development server:**

   ```bash
   npm run dev
   ```

   The frontend will start on `http://localhost:5173`

## API Endpoints

### Authentication

- `POST /auth/signup` - User registration (sends OTP)
- `POST /auth/verify-otp` - Verify OTP and complete registration
- `POST /auth/login` - User login

### Notes

- `GET /api/notes/user/{userId}` - Get all notes for a user
- `POST /api/notes` - Create a new note
- `PUT /api/notes/{id}` - Update a note
- `DELETE /api/notes/{id}` - Delete a note
- `GET /api/notes/user/{userId}/search?q={query}` - Search notes

## User Flow

1. **Landing Page** (`/`) - Choose between Login or Signup
2. **Signup** (`/signup`) - Enter username, email, and password
3. **OTP Verification** (`/otp-verification`) - Verify email with OTP
4. **Login** (`/login`) - Sign in with email and password
5. **Notes Dashboard** (`/notes`) - Manage your notes with full CRUD operations

## Authentication Flow

1. User signs up with username, email, and password
2. Backend sends OTP to the provided email
3. User verifies OTP to complete registration
4. User can then login with email and password
5. Backend returns JWT tokens (access + refresh)
6. Frontend stores tokens and manages authentication state
7. Protected routes require valid authentication

## Environment Configuration

### Backend (application.properties)

```properties
server.port=8080
spring.datasource.url=your_postgresql_url
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.data.mongodb.uri=your_mongodb_uri
spring.mail.username=your_email
spring.mail.password=your_email_password
jwt.secretKey=your_jwt_secret
```

### Frontend (.env)

```
VITE_API_BASE_URL=http://localhost:8085
```

**Security Note**: Never commit `.env` files to version control. Use `.env.example` for documentation.

## Development Notes

- The backend uses both PostgreSQL (for user data) and MongoDB (for notes)
- CORS is configured to allow requests from `http://localhost:5173` and `http://localhost:8080`
- JWT tokens are stored in HTTP-only cookies for security
- The frontend uses React Context for authentication state management
- All API calls include proper error handling and user feedback

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is for educational purposes.
