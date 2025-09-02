# JWT Authentication Implementation

This document describes the JWT (JSON Web Token) authentication implementation added to the DockerIQ Service.

## Overview

The application now includes JWT-based authentication with the following features:
- User login with email/password
- JWT token generation and validation
- Role-based access control
- Password encryption using BCrypt
- Stateless authentication

## API Endpoints

### Authentication Endpoints

#### 1. Login
- **URL**: `POST /auth/login`
- **Body**:
```json
{
    "email": "katarideepak@gmail.com",
    "password": "hello123"
}
```
- **Response**:
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "katarideepak@gmail.com",
    "role": "supervisor",
    "firstName": "deepak",
    "lastName": "k"
}
```

#### 2. Token Validation
- **URL**: `GET /auth/validate`
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `"Token is valid"` or error message

### Protected Endpoints

#### User Management (Requires SUPERVISOR role)
- `GET /users` - Get all users
- `POST /users` - Create new user
- `GET /users/{email}` - Get user by email
- `PUT /users/{email}` - Update user
- `DELETE /users/{email}` - Delete user

#### Test Endpoints
- `GET /test/public` - Public endpoint (no authentication required)
- `GET /test/secured` - Secured endpoint (requires valid JWT token)

## Security Configuration

### JWT Configuration
- **Secret Key**: Configured in `application.properties` as `jwt.secret`
- **Token Expiration**: 24 hours (86400000 ms) by default
- **Algorithm**: HS256

### Access Control
- `/auth/**` - Public access
- `/actuator/**` - Public access (health checks)
- `/health/**` - Public access
- `/test/public` - Public access
- `/users/**` - Requires SUPERVISOR role
- `/test/**` - Requires authentication
- All other endpoints - Require authentication

## Usage Examples

### 1. Login and Get Token
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "katarideepak@gmail.com",
    "password": "hello123"
  }'
```

### 2. Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/test/secured \
  -H "Authorization: Bearer <your-jwt-token>"
```

### 3. Access User Management
```bash
curl -X GET http://localhost:8080/users \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Implementation Details

### Key Components

1. **JwtTokenUtil**: Handles JWT token generation and validation
2. **JwtAuthenticationFilter**: Intercepts requests and validates JWT tokens
3. **AuthService**: Manages authentication logic and password encoding
4. **SecurityConfig**: Configures Spring Security with JWT support
5. **AuthController**: Exposes authentication endpoints

### Password Security
- All passwords are encrypted using BCrypt before storage
- The default super user password is automatically encoded during initialization
- Password updates automatically trigger re-encoding

### Token Management
- Tokens are stateless and stored in the Authorization header
- Token format: `Bearer <jwt-token>`
- Expired tokens are automatically rejected

## Configuration

### Required Properties
```properties
jwt.secret=your-secret-key-here-make-it-long-and-secure-in-production
jwt.expiration=86400000
```

### Security Considerations
1. **Change the default JWT secret** in production
2. **Use HTTPS** in production environments
3. **Implement token refresh** for long-running applications
4. **Add rate limiting** for authentication endpoints
5. **Log authentication attempts** for security monitoring

## Testing

1. Start the application: `./mvnw spring-boot:run`
2. Test public endpoints: `GET /test/public`
3. Login to get a token: `POST /auth/login`
4. Use the token to access protected endpoints
5. Verify role-based access control

## Troubleshooting

### Common Issues
1. **401 Unauthorized**: Check if JWT token is valid and not expired
2. **403 Forbidden**: Verify user has required role for the endpoint
3. **500 Internal Server Error**: Check JWT secret configuration and token format

### Debug Mode
Enable debug logging in `application.properties`:
```properties
logging.level.com.dockeriq.service=DEBUG
logging.level.org.springframework.security=DEBUG
```
