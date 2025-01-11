package ru.mishazx.shortlinkspring.model;

import jakarta.persistence.*;
import lombok.*;
import ru.mishazx.shortlinkspring.model.enums.AuthProvider;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @Column(name = "total_clicks")
    private Long totalClicks = 0L;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Url> urls = new ArrayList<>();

    public void incrementTotalClicks() {
        if (this.totalClicks == null) {
            this.totalClicks = 0L;
        }
        this.totalClicks++;
    }
} 