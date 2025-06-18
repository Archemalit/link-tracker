package backend.academy.scrapper.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "link", indexes = @Index(name = "index_link_url", columnList = "url"))
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

    //    @ManyToMany
    //    @JoinTable(name = "link_chat",
    //        joinColumns = @JoinColumn(name = "link_id", referencedColumnName = "id"),
    //        inverseJoinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"))
    //    private Set<Chat> chats;

    //    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private Set<Tag> tags;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link link)) return false;
        return Objects.equals(url, link.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
