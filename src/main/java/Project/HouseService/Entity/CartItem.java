// src/main/java/Project/HouseService/Entity/CartItem.java
package Project.HouseService.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items",
        indexes = {
                @Index(name="ix_ci_cart", columnList="cart_id"),
                @Index(name="ix_ci_vendor_service", columnList="vendor_service_id"),
                @Index(name="ix_ci_vendor", columnList="vendor_id")
        })
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cart
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ci_cart"))
    private Cart cart;

    // VendorService
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_service_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ci_vendor_service"))
    private VendorService vendorService;

    // VendorProfile: FK(cart_items.vendor_id) -> vendor_profile.user_id  (KHẮC PHỤC LỖI)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", referencedColumnName = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ci_vendor"))
    private VendorProfile vendor;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice = 0L;

    @Column(nullable = false)
    private Long subtotal = 0L;

    @Column(name = "schedule_at")
    private LocalDateTime scheduleAt;

    @Column(name = "address_snapshot", length = 500)
    private String addressSnapshot;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate(){ this.updatedAt = LocalDateTime.now(); }

    public CartItem(){}

    // --- getters/setters ---
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public Cart getCart(){ return cart; }
    public void setCart(Cart cart){ this.cart = cart; }

    public VendorService getVendorService(){ return vendorService; }
    public void setVendorService(VendorService vendorService){ this.vendorService = vendorService; }

    public VendorProfile getVendor(){ return vendor; }
    public void setVendor(VendorProfile vendor){ this.vendor = vendor; }

    public Integer getQuantity(){ return quantity; }
    public void setQuantity(Integer quantity){ this.quantity = quantity; }

    public Long getUnitPrice(){ return unitPrice; }
    public void setUnitPrice(Long unitPrice){ this.unitPrice = unitPrice; }

    public Long getSubtotal(){ return subtotal; }
    public void setSubtotal(Long subtotal){ this.subtotal = subtotal; }

    public LocalDateTime getScheduleAt(){ return scheduleAt; }
    public void setScheduleAt(LocalDateTime scheduleAt){ this.scheduleAt = scheduleAt; }

    public String getAddressSnapshot(){ return addressSnapshot; }
    public void setAddressSnapshot(String addressSnapshot){ this.addressSnapshot = addressSnapshot; }

    public String getNotes(){ return notes; }
    public void setNotes(String notes){ this.notes = notes; }

    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt(){ return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt){ this.updatedAt = updatedAt; }
}
