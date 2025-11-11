package com.ilovepc.project_home.jwt.util;

import com.ilovepc.project_home.config.security.vo.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORITIES_KEY = "auth"; // Role 정보 key

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;
    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private Key key; //서명, 검증에 사용할 객체

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    //엑세스 토큰 생성
    public String createAccessToken(String email, List<Role> roles) {
        return createToken(email, roles, accessTokenValidity);
    }

    //리프레시 토큰 생성
    public String createRefreshToken(String email, List<Role> roles) {
        return createToken(email, roles, refreshTokenValidity);
    }

    //헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7); // "Bearer " 공백포함 7자를 잘라내고 그 뒤의 순수한 토큰 문자 (xxxx.yyyy.zzzz~)만 추출한다.
        }
        return null;
    }
    
    //토큰 검증
    public boolean validateToken(String token) {
        try{
            //서명검증, 만료시간 검증을 통해 유효한 토큰인지 판별
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch(SecurityException | MalformedJwtException e){
            log.warn("Invalid JWT signature");
        }catch(ExpiredJwtException e){
            log.warn("Expired JWT Token");
        }catch(UnsupportedJwtException e){
            log.warn("Unsupported JWT Token");
        }catch(IllegalArgumentException e){
            log.warn("JWT claims string is empty.");
        }
        return false;
    }

    //토큰에서 인증정보 조회
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        if(claims.get(AUTHORITIES_KEY) == null){
            // Access Token에 Role 정보가 없는 경우 (잘못된 토큰)
            throw new RuntimeException("Token without authorization information");
        }
        //클레임에서 권한정보 가져오기
        Collection<?  extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(""))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
        UserDetails principal = new User(claims.getSubject(),"", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    //토큰에서 Claims 추출
    private Claims parseClaims(String token) {
        try{
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        }catch(ExpiredJwtException e){
            //만료된 토큰이어도 Claims는 반환 (재발급 시 사용자이름 필요함)
            return e.getClaims();
        }
    }

    //토큰에서 사용자 이름 추출
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    //토큰 만들어주는 메소드
    private String createToken(String email, List<Role> roles, long validity) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
                .setSubject(email)   //토큰 주체
                .setIssuedAt(now)       //토큰 발급시간
                .setExpiration(expiration) //토큰 만료시간
                .signWith(key, SignatureAlgorithm.HS512);

        // Role이 있다면 claims에 추가한다.
        if(roles != null && !roles.isEmpty()) {
            String authorities = roles.stream()
                    .map(Role::name)
                    .collect(Collectors.joining(","));
            builder.claim(AUTHORITIES_KEY, authorities); //커스텀 클레임을 추가
        }

        return builder.compact(); // 모든 정보를 조합하여 xxxx.yyyyy.zzzz 형태의 문자열을 만든다.
    }


}
