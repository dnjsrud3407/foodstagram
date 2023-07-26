package com.foodstagram.repository;

import com.foodstagram.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByEmail(String email);

    User findByLoginIdAndEmailAndIsDel(String loginId, String email, Boolean isDel);

    @Query("select u.loginId from User u where u.email = :email and u.isDel = :isDel")
    Optional<String> findLoginIdByEmailAndIsDel(@Param("email") String email, @Param("isDel") Boolean isDel);
}
