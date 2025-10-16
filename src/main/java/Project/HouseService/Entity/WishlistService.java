package Project.HouseService.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "customer_wishlist",
        indexes = {
                @Index(name = "idx_cw_customer_created", columnList = "customer_id, created_at"),
                @Index(name = "idx_cw_service", columnList = "vendor_service_id")
        }
)
public class WishlistService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer là user đã đăng nhập
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cw_customer"))
    private User customer;

    // Dịch vụ cụ thể của vendor
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_service_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cw_vendor_service"))
    private VendorService vendorService;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public WishlistService() { }

    public WishlistService(User customer, VendorService vendorService) {
        this.customer = customer;
        this.vendorService = vendorService;
    }

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public VendorService getVendorService() { return vendorService; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setCustomer(User customer) { this.customer = customer; }
    public void setVendorService(VendorService vendorService) { this.vendorService = vendorService; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WishlistService that)) return false;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
