package com.foodstagram.config.auth;

import com.foodstagram.entity.User;
import com.foodstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 일반 회원이 로그인했을 때
 * /login 요청이 오면 UserDetialsService.loadUserByUsername() 이 실행됨.
 * 로그인 진행이 완료되면 security session 내부에 Authentication(PrincipleDetails - UserDetails)가 생성됨.
 */
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageSource ms;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        System.out.println("loginId = " + loginId);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new InternalAuthenticationServiceException(ms.getMessage("notFound.user", null, null)));

        return new PrincipalDetails(user);
    }
}
