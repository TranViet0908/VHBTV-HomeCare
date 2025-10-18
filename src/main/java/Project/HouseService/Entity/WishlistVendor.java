package Project.HouseService.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "customer_wishlist_vendor",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cwv_customer_vendor", columnNames = {"customer_id", "vendor_id"})
        },
        indexes = {
                @Index(name = "idx_cwv_customer_created", columnList = "customer_id, created_at"),
                @Index(name = "idx_cwv_vendor", columnList = "vendor_id")
        }
)
public class WishlistVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer là user đã đăng nhập
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cwv_customer"))
    private User customer;

    // Vendor chính là user có role VENDOR
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cwv_vendor"))
    private User vendor;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public WishlistVendor() { }

    public WishlistVendor(User customer, User vendor) {
        this.customer = customer;
        this.vendor = vendor;
    }

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public User getVendor() { return vendor; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setCustomer(User customer) { this.customer = customer; }
    public void setVendor(User vendor) { this.vendor = vendor; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WishlistVendor that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
