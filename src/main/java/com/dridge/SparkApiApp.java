package com.dridge;

import static spark.Spark.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import io.jsonwebtoken.Jwts;

public class SparkApiApp {
    // Encoded secret available at jwt.io
    private static String SECRET;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String BEARER = "Bearer ";
    private static final Logger logger = LoggerFactory.getLogger(SparkApiApp.class);

    public static void main(String[] args) {
        // Parse command-line argument to set Secret
        if (args.length > 0) {
            SECRET = args[0];
        } else {
            System.err.println("ERROR, usage: java SimpleApp <token>");
            System.exit(1);
        }

        // Define routes
        get("/hello", (req, res) -> "Hello unprotected!");

        get("/protected/hello", (req, res) -> "Hello protected world!");

        // Define login endpoint
        post("/login", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");

            // Replace this with your own authentication logic
            if (username.equals("admin") && password.equals("admin1")) {
                // Generate JWT token
                String authorisedToken = Jwts.builder()
                        .setSubject(username)
                        .signWith(SignatureAlgorithm.HS256, SECRET.getBytes())
                        .compact();
                return authorisedToken;
            } else {
                halt(401, "Unauthorized");
                return null;
            }
        });

        // Add authentication filter for any endpoint behind protected path
        before("/protected/*", (req, res) -> {
            if (req.requestMethod().equals("POST")) {
                String token = req.headers("Authorization");
                if (token == null || !token.startsWith(BEARER)) {
                    halt(401, "Unauthorized");
                } else {
                    try {
                        logger.info("bla");
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

        // Add CORS support
        before((Request request, Response response) -> {
            String origin = request.headers("Origin");
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Allow-Methods", "GET,POST");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            response.header("Access-Control-Allow-Credentials", "true");
            response.type("application/json");
        });
    }
}