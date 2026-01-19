package com.touplus.billing_message.domain.respository;

import com.touplus.billing_message.domain.entity.MessageSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageSnapshotRepository extends JpaRepository<MessageSnapshot, Long> {
}
