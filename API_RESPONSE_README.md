# Generic API Response Class

This document describes how to use the new `ApiResponse<T>` class for consistent API responses across your application.

## Overview

The `ApiResponse<T>` class provides a standardized structure for all API responses, including:
- Success/failure status
- Descriptive messages
- Data payload
- Error details
- Timestamp
- HTTP status code

## Response Structure

```json
{
    "success": true,
    "message": "Operation successful",
    "data": { ... },
    "error": null,
    "timestamp": "2025-09-03T00:00:00",
    "statusCode": 200
}
```

## Usage Examples

### 1. Success Responses

```java
// Simple success with data
return ResponseEntity.ok(ApiResponse.success(userData));

// Success with custom message
return ResponseEntity.ok(ApiResponse.success(userData, "User created successfully"));

// Success without data
return ResponseEntity.ok(ApiResponse.success("Operation completed"));
```

### 2. Error Responses

```java
// Basic error
return ResponseEntity.badRequest()
    .body(ApiResponse.error("Invalid input"));

// Error with custom message
return ResponseEntity.badRequest()
    .body(ApiResponse.error("Invalid input", "Validation failed"));

// Error with custom status code
return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    .body(ApiResponse.error("Invalid token", "Authentication failed", 401));
```

### 3. Predefined Response Types

```java
// Not found (404)
return ResponseEntity.status(HttpStatus.NOT_FOUND)
    .body(ApiResponse.notFound("User not found"));

// Validation error (422)
return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
    .body(ApiResponse.validationError("Email format is invalid"));

// Server error (500)
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body(ApiResponse.serverError("Database connection failed"));
```

## Controller Implementation

```java
@PostMapping("/users")
public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
    try {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdUser, "User created successfully"));
    } catch (ValidationException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.validationError(e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.serverError("Failed to create user"));
    }
}
```

## Benefits

1. **Consistency**: All API responses follow the same structure
2. **Error Handling**: Standardized error response format
3. **Debugging**: Timestamp and status codes for better debugging
4. **Frontend Integration**: Predictable response structure for frontend developers
5. **Maintenance**: Centralized response logic, easier to modify

## Response Types

- **Success**: `success: true`, includes data payload
- **Client Error**: `success: false`, status codes 400-499
- **Server Error**: `success: false`, status codes 500-599
- **Not Found**: `success: false`, status code 404
- **Validation Error**: `success: false`, status code 422

## Example API Responses

### Login Success
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "email": "user@example.com",
        "firstName": "John",
        "lastName": "Doe"
    },
    "error": null,
    "timestamp": "2025-09-03T00:00:00",
    "statusCode": 200
}
```

### Validation Error
```json
{
    "success": false,
    "message": "Validation failed",
    "data": null,
    "error": "Email format is invalid",
    "timestamp": "2025-09-03T00:00:00",
    "statusCode": 422
}
```

### Not Found
```json
{
    "success": false,
    "message": "User not found with email: user@example.com",
    "data": null,
    "error": "Resource not found",
    "timestamp": "2025-09-03T00:00:00",
    "statusCode": 404
}
```
