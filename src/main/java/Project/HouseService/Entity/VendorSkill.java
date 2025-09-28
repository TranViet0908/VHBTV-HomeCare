package Project.HouseService.Entity;

import jakarta.persistence.*;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Entity
@Table(name = "vendor_skill",
        uniqueConstraints = @UniqueConstraint(name = "uk_vendor_skill", columnNames = {"vendor_id", "slug"}))
public class VendorSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lưu trực tiếp FK để truy vấn, phù hợp CSDL
    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "slug", length = 180, nullable = false)
    private String slug;

    // Tham chiếu tuỳ chọn tới VendorProfile qua user_id (unique)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private VendorProfile vendorProfile;

    /* ===== Helpers ===== */
    @PrePersist
    @PreUpdate
    private void ensureSlug() {
        if (this.slug == null || this.slug.isBlank()) {
            this.slug = toSlug(this.name);
        } else {
            this.slug = toSlug(this.slug);
        }
    }

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern DASHES = Pattern.compile("[-]+");

    public static String toSlug(String input) {
        if (input == null) return null;
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = NONLATIN.matcher(normalized).replaceAll("-");
        slug = DASHES.matcher(slug).replaceAll("-");
        slug = slug.toLowerCase(Locale.ROOT);
        slug = slug.replaceAll("^-+", "").replaceAll("-+$", "");
        return slug;
    }

    /* ===== Getters/Setters ===== */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public VendorProfile getVendorProfile() {
        return vendorProfile;
    }

    public void setVendorProfile(VendorProfile vendorProfile) {
        this.vendorProfile = vendorProfile;
    }
}
