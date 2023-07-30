package ru.bogatov.quickmeetmessenger.config;


import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    String SECRET;

    private final static String ROLES = "role";
    private final static String ACCOUNT_CLASS = "class";
    private final static String ALLOWED_CHATS = "allowedChats";
    private final static String ID = "id";

    private final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

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

    public String getUserIdFromToken(String token){
        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        return claims.get(ID).toString();
    }

    public List<UUID> getAllowedChatsIds(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        return (List<UUID>) claims.get(ALLOWED_CHATS);
    }
}
