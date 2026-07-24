# Music Review Platform - Backend

Backend API for a music discovery and review platform. This service handles user authentication, music item management, reviews, likes, searching, and user interactions.

The backend is built with **Spring Boot** and provides REST APIs consumed by the Angular frontend.

## Features

* User registration and authentication
* Login with JWT-based sessions
* User profiles
* Music item management
* Artist, album, and song searching
* Spotify integration
* Create, view, and manage reviews
* Review ratings and descriptions
* Review likes
* User following system
* Personalized review feed
* Search filtering by item type

## Tech Stack

* Java
* Spring Boot
* Spring Data JPA
* Hibernate
* Spring Security
* JWT Authentication
* PostgreSQL
* Maven
* REST API

## Project Structure

```
src/main/java
├── controller
│   └── REST API endpoints
│
├── service
│   └── Business logic
│
├── repository
│   └── Database access
│
├── model
│   └── Database entities
│
├── dto
│   └── Request and response objects
│
└── security
    └── Authentication and authorization
```

## Requirements

Before running the project, make sure you have:

* Java 17+
* Maven
* PostgreSQL database
* Spotify Developer API credentials

## Environment Configuration

Create an `application.properties` or `application.yml` file with the required configuration.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/music_app
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update

jwt.secret=YOUR_SECRET_KEY

spotify.client.id=YOUR_SPOTIFY_CLIENT_ID
spotify.client.secret=YOUR_SPOTIFY_CLIENT_SECRET
```

Do not commit your secrets to GitHub.

## Running the Application

Clone the repository:

```bash
git clone <repository-url>
```

Navigate into the backend directory:

```bash
cd backend
```

Run the application:

```bash
./mvnw spring-boot:run
```

The API will start on:

```
http://localhost:8080
```

## Authentication

Authentication uses JWT tokens.

After login, the API returns a token:

```json
{
  "token": "jwt-token",
  "username": "exampleUser"
}
```

The token must be included in protected requests:

```
Authorization: Bearer <token>
```

## API Overview

### Authentication

| Method | Endpoint         | Description         |
| ------ | ---------------- | ------------------- |
| POST   | `/auth/register` | Register a new user |
| POST   | `/auth/login`    | Login user          |

### Search

| Method | Endpoint          | Description                              |
| ------ | ----------------- | ---------------------------------------- |
| GET    | `/search/{query}` | Search artists, albums, songs, and users |

Example:

```
GET /search/metallica?type=artist,album
```

### Reviews

| Method | Endpoint                   | Description           |
| ------ | -------------------------- | --------------------- |
| POST   | `/reviews`                 | Create a review       |
| GET    | `/reviews/user/{username}` | Get user reviews      |
| GET    | `/reviews/feed/{username}` | Get personalized feed |

### Users

| Method | Endpoint            | Description      |
| ------ | ------------------- | ---------------- |
| GET    | `/users/{username}` | Get user profile |
| POST   | `/users/follow`     | Follow a user    |
| DELETE | `/users/unfollow`   | Remove follow    |

## Database

The application uses PostgreSQL.

Main entities include:

* User
* Review
* Item
* Like
* Follow

Relationships:

* Users can create reviews
* Reviews belong to music items
* Users can like reviews
* Users can follow other users

## Testing

Run tests using:

```bash
./mvnw test
```

## Development Notes

Current development priorities:

* Improve friend recommendation algorithms
* Expand search capabilities
* Add more social features
* Improve automated testing coverage
* Add CI/CD pipeline with GitHub Actions

## Future Improvements

Planned improvements:

* Email verification
* Password reset improvements
* More detailed user statistics
* Better recommendation system
* Production deployment configuration

## License

This project is for educational and personal development purposes.

```

A frontend README can reference this backend and include setup instructions for connecting the Angular application.
```
Ï
