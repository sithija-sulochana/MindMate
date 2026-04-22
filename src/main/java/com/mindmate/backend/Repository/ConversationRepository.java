package com.mindmate.backend.Repository;

import com.mindmate.backend.entities.Conversation;
import com.mindmate.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findByUserOrderByCreatedAtDesc(User user);

    Optional<Conversation> findByIdAndUserId(String id, Long userId);

}
