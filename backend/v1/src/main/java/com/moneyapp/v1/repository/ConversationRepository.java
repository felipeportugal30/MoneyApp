package com.moneyapp.v1.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moneyapp.v1.model.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID>{
    
    List<Conversation> findByUserIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID userId);
}
