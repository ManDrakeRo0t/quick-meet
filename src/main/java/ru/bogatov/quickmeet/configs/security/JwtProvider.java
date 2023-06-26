package ru.bogatov.quickmeet.configs.security;


import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.bogatov.quickmeet.entities.User;
import ru.bogatov.quickmeet.services.user.UserService;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    String SECRET;
    @Value("${jwt.token.access.expire}")
    Integer ACCESS_TOKEN_EXPIRE;
    @Value("${jwt.token.refresh.expire}")
    Integer REFRESH_TOKEN_EXPIRE;
    private final static String PHONE = "phone";
    private final static String ROLES = "roles";
    private final static String ID = "id";

    private final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    private UserService userService;

    public JwtProvider(UserService userService) {
        this.userService = userService;
    }

    public String generateTokenForUser(User user) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(ACCESS_TOKEN_EXPIRE).atZone(ZoneId.systemDefault()).toInstant());
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(PHONE,user.getPhoneNumber());
        claims.put(ROLES,user.getRoleSet());
        claims.put(ID,user.getId());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512,SECRET)
                .compact();
    }

    @Transactional
    public String generateRefreshForUser(User user) {
        Date date = Date.from(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE).atZone(ZoneId.systemDefault()).toInstant());
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(PHONE,user.getPhoneNumber());
        String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512,SECRET)
                .compact();
        user.setRefresh(token);
        userService.updateRefreshToken(user.getId(), token);
        return token;
    }


    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
            return true;
        }catch (ExpiredJwtException expEx) {
            logger.warn("Token expired");
        } catch (UnsupportedJwtException unsEx) {
            logger.warn("Unsupported jwt");
        } catch (MalformedJwtException mjEx) {
            logger.warn("Malformed jwt");
        } catch (SignatureException sEx) {
            logger.warn("Invalid signature");
        } catch (Exception e) {
            logger.warn("invalid token");
        }
        return false;
    }

    public String getLoginFromToken(String token){
        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        return claims.get(PHONE).toString();
    }
}
