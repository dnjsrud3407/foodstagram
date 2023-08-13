package com.foodstagram.service;

import com.foodstagram.dto.UserJoinDto;
import com.foodstagram.entity.User;
import com.foodstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MessageSource ms;

    /**
     * 회원가입
     * @param userJoinDto
     */
    @Transactional
    public Long join(UserJoinDto userJoinDto) {
        // 중복 회원 검증
        validateDuplicateUser(userJoinDto);

        String encodedPw = passwordEncoder.encode(userJoinDto.getPassword());
        User user = User.createUser(userJoinDto.getLoginId(), encodedPw, userJoinDto.getEmail());

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    @Transactional
    public Long joinOauth(UserJoinDto userJoinDto, String provider) {
        String encodedPw = passwordEncoder.encode(userJoinDto.getPassword());
        User user = User.createUser(userJoinDto.getLoginId(), encodedPw, userJoinDto.getEmail(), provider);

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    /**
     * 아이디 & 이메일 중복 확인
     * @param userJoinDto
     */
    private void validateDuplicateUser(UserJoinDto userJoinDto) {
        User byLoginId = userRepository.findByLoginId(userJoinDto.getLoginId()).orElse(null);
        if(byLoginId != null) {
            throw new IllegalStateException(ms.getMessage("duplicate.userJoinForm.loginId", null, null));
        }

        User byEmail = userRepository.findByEmail(userJoinDto.getEmail()).orElse(null);
        if(byEmail != null) {
            throw new IllegalStateException(ms.getMessage("duplicate.userJoinForm.email", null, null));
        }
    }

    /**
     * 아이디 중복 확인
     * @param loginId
     * @return
     */
    public Long validateDuplicateLoginId(String loginId) {
        User findedUser = userRepository.findByLoginId(loginId).orElse(null);

        return findedUser != null ? findedUser.getId() : null;
    }

    /**
     * 이메일 중복 확인
     * @param email
     * @return
     */
    public Long validateDuplicateEmail(String email) {
        User findedUser = userRepository.findByEmail(email).orElse(null);

        return findedUser != null ? findedUser.getId() : null;
    }

    public User findUser(String loginId) {
        return userRepository.findByLoginId(loginId).orElse(null);
    }

    /**
     * 아이디 찾기
     * @param email
     * @return
     */
    public String findLoginId(String email) {
        String loginId = userRepository.findLoginIdByEmailAndIsDel(email, false).orElse(null);

        if(loginId == null) {
            throw new IllegalStateException("일치하는 회원이 없습니다.");
        }

        return loginId;
    }

    /**
     * 비밀번호 변경
     * @param loginId
     * @param newPassword
     * @param email
     */
    @Transactional
    public void changePassword(String loginId, String newPassword, String email) {
        // 만약 oauth 가 null 이 아니면 비밀번호 변경 X
        User findUser = userRepository.findByLoginIdAndEmailAndIsDel(loginId, email, false);
        if(findUser != null) {
            String encodedPw = passwordEncoder.encode(newPassword);
            findUser.changePassword(encodedPw);
        }
    }

    /**
     * 회원 탈퇴
     * @param userId
     * @param password
     */
    @Transactional
    public void deleteUser(Long userId, String password) {
        User findUser = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException());

        // 비밀번호가 일치하지 않으면 오류 발생
        if(findUser.getIsDel().equals(true) || !passwordEncoder.matches(password, findUser.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        findUser.deleteUser();
    }
}
