package org.itrunner.heroes.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.itrunner.heroes.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtTokenUtil {
    private static final String CLAIM_AUTHORITIES = "authorities";

    @Autowired
    private SecurityProperties securityProperties;

    public String generate(UserDetails user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(securityProperties.getJwt().getSecret());
            return JWT.create()
                    .withIssuer(securityProperties.getJwt().getIssuer())
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + securityProperties.getJwt().getExpiration() * 1000))
                    .withSubject(user.getUsername())
                    .withArrayClaim(CLAIM_AUTHORITIES, AuthorityUtil.getAuthorities(user))
                    .sign(algorithm);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public UserDetails verify(String token) {
        if (token == null) {
            throw new JWTVerificationException("token should not be null");
        }

        Algorithm algorithm = Algorithm.HMAC256(securityProperties.getJwt().getSecret());
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(securityProperties.getJwt().getIssuer()).build();
        DecodedJWT jwt = verifier.verify(token);
        return new User(jwt.getSubject(), "N/A", AuthorityUtil.createGrantedAuthorities(jwt.getClaim(CLAIM_AUTHORITIES).asArray(String.class)));
    }
}