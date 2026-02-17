package com.maru.repository.message;

import com.maru.domain.message.MessageBroadcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageBroadcastRepository extends JpaRepository<MessageBroadcast, String> {

    Page<MessageBroadcast> findByDojangIdOrderByCreatedAtDesc(String dojangId, Pageable pageable);
}
