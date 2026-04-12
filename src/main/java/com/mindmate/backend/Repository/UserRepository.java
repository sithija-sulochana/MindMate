package com.mindmate.backend.Repository;

import com.mindmate.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
     Optional<User> findByClerkId(String clerkId);
}
