# Spark Api App

This is a simple Java Spark application that demonstrates how to add JWT token-based authentication to your Spark application.

This application creates a Spark API with endpoints for unprotected and protected routes. It includes a login endpoint that generates a JSON Web Token (JWT) if the user's credentials are authenticated. It also includes an authentication filter for endpoints behind the protected path that verifies the presence and validity of the JWT token. The application also adds Cross-Origin Resource Sharing (CORS) support to handle requests from different domains.

## Requirements

To run this application, you need to have Java 8 or later installed. Please note that the pom specifies Java 16. Additionally, you must have the following libraries installed:

- Spark
- Jackson
- JWT

## Usage

1. Compile the application using your preferred Java IDE or build tool
2. Run the application with the following command: `java SparkApiApp <SECRET>`
    - `<SECRET>` should be replaced with your secret string used for JWT token generation
3. The application will start up and will listen for incoming requests on the default port (4567)
4. You can use the following endpoints:

    - `GET /hello` - an unprotected endpoint that returns a "Hello unprotected!" message
    - `GET /protected/hello` - a protected endpoint that requires a valid JWT token in the Authorization header in the format "Bearer <token>" and returns a "Hello protected world!" message
    - `POST /login` - an endpoint that takes a `username` and `password` as query parameters, and returns a JWT token if the authentication is successful

## Configuration

The application requires a secret string to generate JWT tokens. This secret should be provided as a command-line argument when starting the application.

```java
if (args.length > 0) {
    SECRET = args[0];
} else {
    System.err.println("ERROR, usage: java SparkApiApp <token>");
    System.exit(1);
}
```

The application also supports Cross-Origin Resource Sharing (CORS). You can configure allowed origins, methods, and headers by modifying the `before` filter at the end of the `main` method.

```java
before((Request request, Response response) -> {
    String origin = request.headers("Origin");
    response.header("Access-Control-Allow-Origin", origin);
    response.header("Access-Control-Allow-Methods", "GET,POST");
    response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
    response.header("Access-Control-Allow-Credentials", "true");
    response.type("application/json");
});
```

## Security

The application uses JWT tokens to secure the protected endpoints. To obtain a JWT token, a client should authenticate with the `/login` endpoint by providing a valid username and password. If the authentication is successful, the endpoint will return a JWT token that can be used for subsequent requests.

The protected endpoints are secured with an authentication filter that checks if a valid JWT token is present in the `Authorization` header. If a valid token is not present or is invalid, the endpoint will return a `401 Unauthorized` response.

```java
before("/protected/*", (req, res) -> {
    if (req.requestMethod().equals("POST")) {
        String token = req.headers("Authorization");
        if (token == null || !token.startsWith(BEARER)) {
            halt(401, "Unauthorized");
        } else {
            try {
                Jws<Claims> claims = Jwts.parserBuilder()
                        // The key used must be hmacsha encoded
                        .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
                        .build()
                        .parseClaimsJws(token.substring(BEARER.length()));

                req.attribute("claims", claims.getBody());
            } catch (Exception e) {
                halt(401, "Unauthorized!");
            }
        }
    }
});
```

## License

This project is licensed under the MIT License.