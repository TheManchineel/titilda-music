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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

public final class Authentication {
    private Authentication() {}

    public static class UserAlreadyExistsException extends Exception {
        public UserAlreadyExistsException() {
            super();
        }
    }

    public static class UserCreationFailureException extends Exception {
        public UserCreationFailureException() {
            super();
        }
    }

    private static final String TOKEN_SECRET = ConfigManager.getString(ConfigManager.ConfigKey.AUTH_SECRET);

    private static boolean validatePassword(String pass, String digest) {
        return Password.check(pass, digest).withArgon2();
    }

    private static String hashPassword(String pass) {
        return Password.hash(pass).addRandomSalt(16).withArgon2().getResult();
    }

    public static String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        Date validityStartDate = new Date();
        Date validityEndDate = new Date(validityStartDate.getTime() + 1000L * 3600 * 24 * 30); // 30 days
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

    public static String registerUser(String fullName, String username, String password) throws UserAlreadyExistsException, UserCreationFailureException {
        try (Connection con = DatabaseManager.getConnection()) {
            DAO dao = new DAO(con);
            if (dao.getUserByUsername(username).isPresent()) {
                throw new UserAlreadyExistsException();
            }
            String hashedPassword = hashPassword(password);
            Date firstLogin = new Date();
            User newUser = new User(username, hashedPassword, fullName, new Timestamp(firstLogin.getTime()));
            dao.insertUser(newUser);
            return generateToken(newUser);
        }
        catch (SQLException e) {
            throw new UserCreationFailureException();
        }
    }
}
