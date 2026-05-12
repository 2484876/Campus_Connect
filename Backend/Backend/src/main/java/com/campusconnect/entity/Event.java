package com.campusconnect.entity;

import com.campusconnect.enums.EventCategory;
import com.campusconnect.enums.EventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "event_end_date")
    private LocalDateTime eventEndDate;

    @Column(length = 200)
    private String location;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 20)
    private EventType eventType = EventType.PHYSICAL;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EventCategory category = EventCategory.OTHER;

    @Column(name = "virtual_link", length = 500)
    private String virtualLink;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "show_attendees")
    private boolean showAttendees = true;

    @Column(name = "reminder_sent")
    private boolean reminderSent = false;

    @Column(name = "is_active")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}