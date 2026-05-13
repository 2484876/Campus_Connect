package com.campusconnect.repository;

import com.campusconnect.entity.Connection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    @Query("SELECT c FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED'")
    Page<Connection> findAcceptedConnections(@Param("uid") Long userId, Pageable pageable);

    @Query("SELECT c FROM Connection c WHERE c.receiver.id = :uid AND c.status = 'PENDING'")
    Page<Connection> findPendingRequests(@Param("uid") Long userId, Pageable pageable);

    @Query("SELECT c FROM Connection c WHERE c.sender.id = :uid AND c.status = 'PENDING'")
    Page<Connection> findSentRequests(@Param("uid") Long userId, Pageable pageable);

    @Query("SELECT c FROM Connection c WHERE (c.sender.id = :u1 AND c.receiver.id = :u2) OR (c.sender.id = :u2 AND c.receiver.id = :u1)")
    Optional<Connection> findBetweenUsers(@Param("u1") Long u1, @Param("u2") Long u2);

    @Query("SELECT COUNT(c) FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED'")
    int countAcceptedConnections(@Param("uid") Long userId);

    @Query("SELECT CASE WHEN c.sender.id = :uid THEN c.receiver.id ELSE c.sender.id END FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED'")
    List<Long> findConnectedUserIds(@Param("uid") Long userId);

    @Query(value =
            "SELECT DISTINCT mu.id FROM users mu WHERE mu.id IN ( " +
                    "  SELECT CASE WHEN c1.sender_id = :u1 THEN c1.receiver_id ELSE c1.sender_id END " +
                    "  FROM connections c1 " +
                    "  WHERE (c1.sender_id = :u1 OR c1.receiver_id = :u1) AND c1.status = 'ACCEPTED' " +
                    ") AND mu.id IN ( " +
                    "  SELECT CASE WHEN c2.sender_id = :u2 THEN c2.receiver_id ELSE c2.sender_id END " +
                    "  FROM connections c2 " +
                    "  WHERE (c2.sender_id = :u2 OR c2.receiver_id = :u2) AND c2.status = 'ACCEPTED' " +
                    ")",
            nativeQuery = true)
    List<Long> findMutualConnectionIds(@Param("u1") Long u1, @Param("u2") Long u2);
}