package org.titilda.music.base.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.password4j.Password;
import org.titilda.music.base.database.DAO;
import org.titilda.music.base.database.DatabaseManager;
import org.titilda.music.base.model.User;
import org.titilda.music.base.util.ConfigManager;

import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

public final class Authentication {
    private Authentication() {}

    private static final String TOKEN_SECRET = ConfigManager.getString(ConfigManager.ConfigKey.AUTH_SECRET);

    private static boolean validatePassword(String pass, String digest) {
        return Password.check(pass, digest).withArgon2();
    }

    public static String hashPassword(String pass) {
        return Password.hash(pass).addRandomSalt(16).withArgon2().getResult();
    }

    public static String generateToken(User user, Date validityStartDate, Date validityEndDate) {
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuedAt(validityStartDate)
                .withExpiresAt(validityEndDate)
                .sign(algorithm);
    }

    public static Optional<User> validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
            Date validityStartDate = decodedJWT.getIssuedAt();
            String nickname = decodedJWT.getSubject();
            try {
                return new DAO(DatabaseManager.getConnection())
                        .getUserByUsername(decodedJWT.getSubject())
                        .filter(user -> (!user.getLastSessionInvalidation().after(validityStartDate)));
            }
            catch (SQLException e) {
                System.out.println("Error getting user by username: " + nickname);
            }
        } catch (JWTVerificationException e) {
            System.out.println("Invalid token: " + token + " - " + e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<User> validateCredentials(String username, String password) {
        try {
            return new DAO(DatabaseManager.getConnection())
                    .getUserByUsername(username)
                    .filter(user -> validatePassword(password, user.getPasswordHash()));
        }
        catch (SQLException e) {
            System.out.println("Error getting user by username: " + username);
            return Optional.empty();
        }
    }
}
