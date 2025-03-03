package io.hency.aisuperapp.common.util;

import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtUtils {

    @SneakyThrows
    public static Claims parseClaims(String token) {
        String[] splitToken = token.split("\\.");
        if (splitToken.length < 2) {
            throw new IllegalAccessException("Invalid token");
        }
        String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

        try {
            return Jwts.parser()
                    .parseClaimsJwt(unsignedToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("From JwtUtils.parseClaims: {}", e.getMessage());
            throw new TokenExpiredException(ErrorCode.H500C);
        }
    }
}
