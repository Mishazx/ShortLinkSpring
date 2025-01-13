package ru.mishazx.shortlinkspring.model;

import jakarta.persistence.*;
import lombok.*;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String username;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @Builder.Default
    private Long totalClicks = 0L;

    @Builder.Default
    private Long createdUrls = 0L;

    @Builder.Default
    private Long activeUrls = 0L;

    public void incrementTotalClicks() {
        this.totalClicks++;
    }

    public void incrementCreatedUrls() {
        this.createdUrls++;
        this.activeUrls++;
    }

    public void decrementCreatedUrls() {
        if (this.activeUrls > 0) {
            this.activeUrls--;
        }
    }
} 