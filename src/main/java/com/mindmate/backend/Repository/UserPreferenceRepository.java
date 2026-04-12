package com.mindmate.backend.Repository;



import com.mindmate.backend.entities.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {


    List<UserPreference> findByUserId(Long userId);


    @Query("SELECT p FROM UserPreference p WHERE p.user.clerkId = :clerkId")
    List<UserPreference> findAllByClerkId(@Param("clerkId") String clerkId);


    @Query("SELECT p FROM UserPreference p WHERE p.user.id = :userId AND p.prefKey = :key")
    Optional<UserPreference> findByUserIdAndPrefKey(@Param("userId") Long userId, @Param("key") String key);
}