package com.campusconnect.repository;
import com.campusconnect.entity.Connection;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    @Query("SELECT c FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED'")
    Page<Connection> findAcceptedConnections(@Param("uid") Long userId, Pageable pageable);
    @Query("SELECT c FROM Connection c WHERE c.receiver.id = :uid AND c.status = 'PENDING'")
    Page<Connection> findPendingRequests(@Param("uid") Long userId, Pageable pageable);
    @Query("SELECT c FROM Connection c WHERE (c.sender.id = :u1 AND c.receiver.id = :u2) OR (c.sender.id = :u2 AND c.receiver.id = :u1)")
    Optional<Connection> findBetweenUsers(@Param("u1") Long u1, @Param("u2") Long u2);
    @Query("SELECT COUNT(c) FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED'")
    int countAcceptedConnections(@Param("uid") Long userId);
}
