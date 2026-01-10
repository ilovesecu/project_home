package com.ilovepc.project_home.config.security;

import com.ilovepc.project_home.common.vo.CommonErrCode;
import com.ilovepc.project_home.config.security.vo.HomeProjectUserDetails;
import com.ilovepc.project_home.config.security.vo.User;
import com.ilovepc.project_home.repository.AuthMasterMapper;
import com.ilovepc.project_home.common.utils.ClientUtils;
import com.ilovepc.project_home.common.exception.AuthenticationFailException;
import com.ilovepc.project_home.web.auth.vo.signin.SignInParam;
import com.ilovepc.project_home.web.auth.vo.signin.SignInResult;
import com.ilovepc.project_home.web.auth.vo.signin.SignInRetValCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeProjectUserDetailsService implements UserDetailsService {
    private final AuthMasterMapper authMasterMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 현재 HTTP 요청 객체 가져오기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // 2. IP 주소 추출
        String clientIp = ClientUtils.getClientIp(request);

        SignInResult signInResult = authMasterMapper.pSignIn(SignInParam.builder()
                    .email(username)
                    .memIp(clientIp)
                    .build());
        if(signInResult.getResultCode() != SignInRetValCode.SUCCESS){
            //로그인 되지 않고  SignInRetValCode에 따른 에러 메시지 전송
            CommonErrCode errCode = switch(signInResult.getResultCode()){
                case ID_NOT_FOUND -> CommonErrCode.USER_NOT_FOUND;
                case INACTIVE_USER -> CommonErrCode.USER_INACTIVE;
                case IP_BLOCK -> CommonErrCode.IP_SUSPENDED;
                case REST_USER -> CommonErrCode.USER_SUSPENDED;
                default -> CommonErrCode.UNKNOWN_ERROR;
            };
            throw new AuthenticationFailException(errCode);
        }
        //3. 정상유저 조회
        User user = authMasterMapper.pUserSel(signInResult.getUserNo());
        return createUserDetails(user);
    }

    private UserDetails createUserDetails(User user) {
        return HomeProjectUserDetails.builder()
                .userNo(user.getUserNo())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .createdAt(user.getCreatedAt())
                .emailVerifiedYn(user.getEmailVerifiedYn())
                .authorities(createAuthorities(user))
                .build();
    }

    private List<GrantedAuthority> createAuthorities(User user) {
        //TODO 추후 프로필 사진 체크도 추가!
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        //기본적으로 GUEST 권한 추가
        authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
        
        //이메일 인증 시 USER 권한 추가
        if("y".equals(user.getEmailVerifiedYn())){
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return  authorities;
    }

}
