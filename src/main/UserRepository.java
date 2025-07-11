package com.jpmc.midascore.repository;

import com.jpmc.midascore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
