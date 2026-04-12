package com.mindmate.backend.Repository;

import com.mindmate.backend.entities.Conversation;
import com.mindmate.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserOrderByCreatedAtDesc(User user);

}
