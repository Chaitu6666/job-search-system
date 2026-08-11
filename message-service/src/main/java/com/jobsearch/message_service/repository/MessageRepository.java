package com.jobsearch.message_service.repository;

import com.jobsearch.message_service.entity.Message;
import com.jobsearch.message_service.enums.MessageDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByJobSeekerIdAndEmployerIdOrderBySentAtAsc(Long jobSeekerId, Long employerId);
    List<Message> findByJobSeekerIdAndEmployerIdAndJobIdOrderBySentAtAsc(Long jobSeekerId, Long employerId, Long jobId);
    List<Message> findByJobSeekerIdAndDirection(Long jobSeekerId, MessageDirection direction);
    List<Message> findByJobSeekerIdAndDirectionOrderBySentAtDesc(Long jobSeekerId, MessageDirection direction);
    List<Message> findByEmployerIdAndDirection(Long employerId, MessageDirection direction);
    List<Message> findByEmployerIdAndDirectionOrderBySentAtDesc(Long employerId, MessageDirection direction);
    List<Message> findByJobIdOrderBySentAtAsc(Long jobId);
    long countByJobSeekerIdAndIsReadFalseAndDirection(Long jobSeekerId, MessageDirection direction);
    long countByEmployerIdAndIsReadFalseAndDirection(Long employerId, MessageDirection direction);


    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true " +
    "WHERE m.jobSeekerId = :jobSeekerId " +
    "AND m.employerId = :employerId " +
    "AND m.isRead = false")
    int markThreadAsRead(
            @Param("jobSeekerId") Long jobSeekerId,
            @Param("employerId") Long employerId
    );
}
