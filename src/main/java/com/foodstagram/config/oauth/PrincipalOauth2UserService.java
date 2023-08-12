package com.foodstagram.config.oauth;

import com.foodstagram.config.auth.PrincipalDetails;
import com.foodstagram.config.oauth.provider.KakaoUserInfo;
import com.foodstagram.config.oauth.provider.OAuth2UserInfo;
import com.foodstagram.entity.User;
import com.foodstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2 회원이 로그인했을 때
 * provider에 맞게 정보를 생성하고 User 회원가입 및 로그인 진행.
 * 로그인 진행이 완료되면 security session 내부에 Authentication(PrincipleDetails - OAuth2User)가 생성됨
 */
@Service
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        OAuth2UserInfo oAuth2UserInfo = null;
        String provider = userRequest.getClientRegistration().getRegistrationId();

        if(provider.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String loginId = provider + "_" + providerId;
        String password = passwordEncoder.encode(loginId);
        String email = oAuth2UserInfo.getEmail();

        // 회원가입된 유저인지 확인
        User findUser = userRepository.findByLoginId(loginId).orElse(null);

        if(findUser == null) {
            // 회원가입 진행
            findUser = User.createUser(loginId, password, email, provider);
            userRepository.save(findUser);
        }

        return new PrincipalDetails(findUser, oAuth2User.getAttributes());
    }
}
