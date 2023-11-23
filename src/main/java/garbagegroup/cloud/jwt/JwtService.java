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

@Service
public class JwtService {

    private static final String SECRET_KEY = "thisisourrandomsecretkeythatismorethan16character";

    public  String extractUsername(String token){
        return  extractClaim(token,Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey().getEncoded()).build().parseClaimsJws(token).getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

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


    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }


}
