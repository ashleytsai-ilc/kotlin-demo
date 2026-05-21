package com.example.demo.user.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, String> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByUsernameAndDeletedAtIsNull(String username);

    Optional<UserAccount> findByIdAndDeletedAtIsNull(String id);

    Optional<UserAccount> findByActiveNicknameKey(String activeNicknameKey);

    boolean existsByActiveUsernameKey(String activeUsernameKey);

    boolean existsByActiveNicknameKey(String activeNicknameKey);
}
