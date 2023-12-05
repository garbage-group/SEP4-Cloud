package garbagegroup.cloud.jwt;

import garbagegroup.cloud.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

/**
 * Service class responsible for handling JSON Web Token (JWT) operations, such as token generation, extraction, and validation.
 */
@Service
public class JwtService {

    private static final String SECRET_KEY = "thisisourrandomsecretkeythatismorethan16character";

    /**
     * Extracts the username from the given JWT token.
     *
     * @param token The JWT token from which the username will be extracted.
     * @return The extracted username from the token.
     */
    public  String extractUsername(String token){
        return  extractClaim(token,Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the JWT token using a claims resolver function.
     *
     * @param token          The JWT token containing the claims.
     * @param claimsResolver The function to resolve the desired claim from the token's claims.
     * @param <T>            The type of the resolved claim.
     * @return The resolved claim from the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the provided JWT token.
     *
     * @param token The JWT token from which claims will be extracted.
     * @return The claims extracted from the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey().getEncoded()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Retrieves the signing key used for JWT token verification.
     *
     * @return The signing key for JWT token verification.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates if the provided JWT token is still valid for the given user details.
     *
     * @param token       The JWT token to be validated.
     * @param userDetails The UserDetails object containing user details for validation.
     * @return True if the token is valid for the user details; otherwise, false.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the provided JWT token has expired.
     *
     * @param token The JWT token to check for expiration.
     * @return True if the token has expired; otherwise, false.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    /**
     * Extracts the expiration date from the provided JWT token.
     *
     * @param token The JWT token from which expiration date will be extracted.
     * @return The expiration date extracted from the token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generates a JWT token based on the user details.
     *
     * @param userDetails The User object containing user information.
     * @return The generated JWT token.
     */
    public String generateToken(User userDetails) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 30);
        LocalDate localDate = LocalDate.now().plusDays(1);
        Date expirationDate = Date.from(localDate.atStartOfDay().atZone(calendar.getTimeZone().toZoneId()).toInstant());



        Claims claims = Jwts.claims();
        claims.put(Claims.SUBJECT, userDetails.getUsername());
        claims.put("username", userDetails.getUsername());
        claims.put("fullname", userDetails.getFullname());
        claims.put("role", userDetails.getRole());

        return Jwts.builder().setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
    }


    /**
     * Resolves the JWT token from the HTTP request's Authorization header.
     *
     * @param req The HttpServletRequest containing the authorization header.
     * @return The resolved JWT token from the request header.
     */
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }


}
