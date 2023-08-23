package com.foodstagram.repository;

import com.foodstagram.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByEmail(String email);

    Optional<User> findByLoginIdAndEmail(String loginId, String email);

    @Query("select u.loginId from User u where u.email = :email")
    Optional<String> findLoginIdByEmail(@Param("email") String email);
}
