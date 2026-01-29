package com.maru.repository.message;

import com.maru.domain.message.MessageDispatch;
import com.maru.domain.message.MessageDispatchAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageDispatchAttemptRepository extends JpaRepository<MessageDispatchAttempt, Long> {
    List<MessageDispatchAttempt> findByDispatchIdOrderByAttemptNumberAsc(MessageDispatch dispatch);
}
