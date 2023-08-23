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
    private final FoodService foodService;
    private final ListService listService;
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

    /**
     * 아이디 찾기
     * @param email
     * @return
     */
    public String findLoginId(String email) {
        return userRepository.findLoginIdByEmail(email).orElseThrow(
                () -> new IllegalStateException("일치하는 회원이 없습니다."));
    }

    /**
     * 비밀번호 찾기
     * - 아이디 확인
     * - 이메일 확인
     * - 아이디, 이메일 둘다 확인
     * @param email
     * @return
     */
    public void findPassword(String loginId, String email) {
        User findByLoginId = userRepository.findByLoginId(loginId).orElseThrow(
                () -> new IllegalStateException("notFound.findPwForm.loginId"));

        userRepository.findLoginIdByEmail(email).orElseThrow(
                () -> new IllegalStateException("notFound.findPwForm.email"));

        if(!findByLoginId.getEmail().equals(email)) {
            throw new IllegalStateException("notFoundUser");
        }
    }

    /**
     * 비밀번호 찾기 후 변경
     * @param loginId
     * @param email
     * @param newPassword
     */
    @Transactional
    public void changePassword(String loginId, String email, String newPassword) {
        User findUser = userRepository.findByLoginIdAndEmail(loginId, email).orElse(null);
        if(findUser != null) {
            findUser.changePassword(passwordEncoder.encode(newPassword));
        }
    }

    /**
     * 마이 페이지에서 비밀번호 변경
     * @param userId
     * @param newPassword
     */
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User findUser = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException());

        findUser.changePassword(passwordEncoder.encode(newPassword));
    }

    /**
     * 회원 탈퇴
     * @param userId
     */
    @Transactional
    public void deleteUser(Long userId) {
        User findUser = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException());

        // user table 수정
        findUser.deleteUser();

        // food, food_category, food_picture 비활성화
        foodService.deleteFoodByDeleteUser(userId);

        // list 비활성화
        listService.deleteListByDeleteUser(userId);
    }
}
