// src/main/java/Project/HouseService/Service/Customer/ProfileService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.CustomerProfile;
import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Entity.User;
import Project.HouseService.Repository.CustomerProfileRepository;
import Project.HouseService.Repository.ServiceOrderItemRepository;
import Project.HouseService.Repository.ServiceOrderRepository;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.*;
import java.io.IOException;

@Service
@Transactional
public class ProfileService {

    @PersistenceContext
    private EntityManager em;

    private final UserRepository userRepo;
    private final CustomerProfileRepository profileRepo;
    private final ServiceOrderRepository orderRepo;
    private final ServiceOrderItemRepository orderItemRepo;
    private final VendorProfileRepository vendorProfileRepo;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepo,
                          CustomerProfileRepository profileRepo,
                          ServiceOrderRepository orderRepo,
                          ServiceOrderItemRepository orderItemRepo,
                          VendorProfileRepository vendorProfileRepo,
                          PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.vendorProfileRepo = vendorProfileRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User requireUserByUsername(String username) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :un", User.class);
        q.setParameter("un", username);
        List<User> list = q.getResultList();
        if (list.isEmpty()) throw new NoSuchElementException("User not found: " + username);
        return list.get(0);
    }

    @Transactional(readOnly = true)
    public CustomerProfile findProfileByUserId(Long userId) {
        TypedQuery<CustomerProfile> q = em.createQuery(
                "SELECT cp FROM CustomerProfile cp WHERE cp.user.id = :uid", CustomerProfile.class);
        q.setParameter("uid", userId);
        List<CustomerProfile> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public CustomerProfile ensureProfile(User user) {
        CustomerProfile cp = findProfileByUserId(user.getId());
        if (cp == null) {
            cp = new CustomerProfile();
            cp.setUser(user);
        }
        return cp;
    }

    public String normalizeAvatarForView(String raw, Long userId) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.replace("\\", "/");

        // đã đúng
        if (s.startsWith("/uploads/")) return s;

        // thiếu dấu /
        if (s.startsWith("uploads/")) return "/" + s;

        // lưu kiểu avatars/xxx.png
        if (s.startsWith("/avatars/")) return "/uploads" + s;             // -> /uploads/avatars/xxx.png
        if (s.startsWith("avatars/"))  return "/uploads/" + s;

        // chỉ có tên file
        if (userId != null) return "/uploads/avatars/" + userId + "/" + s;
        return "/uploads/avatars/" + s;
    }

    public void updateProfile(User user,
                              CustomerProfile cp,
                              String fullName,
                              LocalDate dob,
                              String gender,
                              String addressLine,
                              String email,
                              String phone,
                              String avatarPathOrNull) {

        if (email != null && email.trim().length() > 0) {
            Long cnt = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.email = :e AND u.id <> :id", Long.class)
                    .setParameter("e", email.trim())
                    .setParameter("id", user.getId())
                    .getSingleResult();
            if (cnt != null && cnt > 0) throw new IllegalArgumentException("Email đã được dùng");
            user.setEmail(email.trim());
        }

        if (phone != null && phone.trim().length() > 0) {
            Long cnt = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.phone = :p AND u.id <> :id", Long.class)
                    .setParameter("p", phone.trim())
                    .setParameter("id", user.getId())
                    .getSingleResult();
            if (cnt != null && cnt > 0) throw new IllegalArgumentException("Số điện thoại đã được dùng");
            user.setPhone(phone.trim());
        }

        // Chuẩn hóa URL avatar về dạng /uploads/** để WebConfig hiện tại phục vụ đúng
        if (avatarPathOrNull != null && !avatarPathOrNull.isEmpty()) {
            String p = avatarPathOrNull.trim().replace('\\', '/');

            // Nếu là path tuyệt đối, cắt từ /uploads/ trở đi
            int idx = p.indexOf("/uploads/");
            if (idx >= 0) p = p.substring(idx);

            // Thiếu dấu / đầu
            if (p.startsWith("uploads/")) p = "/" + p;

            // Lỡ lưu "avatars/..." thì thêm prefix "/uploads/"
            if (p.startsWith("avatars/"))  p = "/uploads/" + p;
            if (p.startsWith("/avatars/")) p = "/uploads" + p;

            // Xử lý trùng thư mục
            p = p.replace("/avatars/avatars/", "/avatars/");

            // Nếu vẫn chưa nằm dưới /uploads/, coi như chỉ là tên file -> gắn thư mục theo userId
            if (!p.startsWith("/uploads/")) {
                p = "/uploads/avatars/" + user.getId() + "/" + p.replaceFirst("^/+", "");
            }

            setUserAvatarReflect(user, p); // hỗ trợ setAvatarUrl hoặc setAvatar
        }

        userRepo.save(user);

        if (fullName != null) {
            boolean setOk = invokeIfExists(cp, "setFullName", new Class[]{String.class}, new Object[]{fullName.trim()});
            if (!setOk) invokeIfExists(cp, "setFullname", new Class[]{String.class}, new Object[]{fullName.trim()});
        }
        if (dob != null) cp.setDob(dob);
        if (gender != null && !gender.trim().isEmpty()) setGenderReflect(cp, gender.trim());
        if (addressLine != null) cp.setAddressLine(addressLine.trim());

        profileRepo.save(cp);
    }

    public void changePassword(User user, String currentRaw, String nextRaw) {
        if (currentRaw == null || !passwordEncoder.matches(currentRaw, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        if (nextRaw == null || nextRaw.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải từ 6 ký tự");
        }
        user.setPassword(passwordEncoder.encode(nextRaw));
        userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public List<ServiceOrder> recentOrders(Long customerId, int limit) {
        TypedQuery<ServiceOrder> q = em.createQuery(
                "SELECT o FROM ServiceOrder o WHERE o.customerId = :cid ORDER BY o.createdAt DESC",
                ServiceOrder.class);
        q.setParameter("cid", customerId);
        q.setMaxResults(limit);
        return q.getResultList();
    }

    @Transactional(readOnly = true)
    public Map<Long, String> mapVendorDisplayNameByUserIds(Collection<Long> vendorUserIds) {
        if (vendorUserIds == null || vendorUserIds.isEmpty()) return Collections.emptyMap();

        var q = em.createQuery(
                "SELECT vp.user.id, COALESCE(vp.displayName, vp.legalName) " +
                        "FROM VendorProfile vp WHERE vp.user.id IN :ids", Object[].class);
        q.setParameter("ids", vendorUserIds);
        List<Object[]> rows = q.getResultList();

        Map<Long, String> map = new HashMap<>();
        for (Object[] r : rows) {
            Long uid = ((Number) r[0]).longValue();
            String name = r[1] != null ? r[1].toString() : "N/A";
            map.put(uid, name);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> recentOrderViews(Long customerId, int limit) {
        List<ServiceOrder> orders = recentOrders(customerId, limit);
        Set<Long> vendorIds = orders.stream()
                .map(this::extractVendorIdReflect)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> vendorNames = mapVendorDisplayNameByUserIds(vendorIds);

        List<Map<String, Object>> out = new ArrayList<>();
        for (ServiceOrder o : orders) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", invokeNum(o, "getId"));
            row.put("orderCode", invokeStr(o, "getOrderCode"));
            row.put("status", invokeStr(o, "getStatus"));
            row.put("total", invokeNum(o, "getTotal"));
            row.put("createdAt", invokeObj(o, "getCreatedAt"));
            Long vid = extractVendorIdReflect(o);
            row.put("vendorId", vid);
            row.put("vendorName", vid != null ? vendorNames.getOrDefault(vid, "N/A") : "N/A");
            out.add(row);
        }
        return out;
    }

    // ---------- helpers ----------

    private void setUserAvatarReflect(User user, String path) {
        // ưu tiên setAvatarUrl(String), fallback setAvatar(String)
        if (!invokeIfExists(user, "setAvatarUrl", new Class[]{String.class}, new Object[]{path})) {
            invokeIfExists(user, "setAvatar", new Class[]{String.class}, new Object[]{path});
        }
    }

    private void setGenderReflect(CustomerProfile cp, String gender) {
        try {
            // tìm method setGender(*) và xử lý theo kiểu tham số
            for (Method m : cp.getClass().getMethods()) {
                if (!m.getName().equals("setGender") || m.getParameterCount() != 1) continue;
                Class<?> t = m.getParameterTypes()[0];
                if (t.isEnum()) {
                    @SuppressWarnings("unchecked")
                    Object ev = Enum.valueOf((Class<Enum>) t, gender.toUpperCase());
                    m.invoke(cp, ev);
                    return;
                } else if (t == String.class) {
                    m.invoke(cp, gender);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    private Long extractVendorIdReflect(ServiceOrder o) {
        // ưu tiên getVendorId(), fallback getVendor()->getId()
        Object v = invokeObj(o, "getVendorId");
        if (v instanceof Number) return ((Number) v).longValue();
        Object vendorObj = invokeObj(o, "getVendor");
        if (vendorObj != null) {
            Object id = invokeObj(vendorObj, "getId");
            if (id instanceof Number) return ((Number) id).longValue();
        }
        return null;
    }

    private boolean invokeIfExists(Object target, String name, Class<?>[] types, Object[] args) {
        try {
            Method m = target.getClass().getMethod(name, types);
            m.invoke(target, args);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Object invokeObj(Object target, String name) {
        try {
            Method m = target.getClass().getMethod(name);
            return m.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private String invokeStr(Object target, String name) {
        Object o = invokeObj(target, name);
        return o != null ? o.toString() : null;
    }

    private Long invokeNum(Object target, String name) {
        Object o = invokeObj(target, name);
        return (o instanceof Number) ? ((Number) o).longValue() : null;
    }
    @Transactional(readOnly = true)
    public long countOrders(Long customerId) {
        Long c = em.createQuery(
                        "SELECT COUNT(o) FROM ServiceOrder o WHERE o.customerId = :cid", Long.class)
                .setParameter("cid", customerId)
                .getSingleResult();
        return c != null ? c : 0L;
    }

    @Transactional(readOnly = true)
    public long countOrdersByStatus(Long customerId, String status) {
        Long c = em.createQuery(
                        "SELECT COUNT(o) FROM ServiceOrder o WHERE o.customerId = :cid AND o.status = :st", Long.class)
                .setParameter("cid", customerId)
                .setParameter("st", status)
                .getSingleResult();
        return c != null ? c : 0L;
    }

    @Transactional(readOnly = true)
    public long countWishlist(Long customerId) {
        Number n = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM customer_wishlist WHERE customer_id = :cid")
                .setParameter("cid", customerId)
                .getSingleResult();
        return n != null ? n.longValue() : 0L;
    }

    @Transactional(readOnly = true)
    public String buildAvatarUrl(User user) {
        if (user == null) return null;

        String raw = null;
        try {
            raw = (String) user.getClass().getMethod("getAvatarUrl").invoke(user);
        } catch (Exception ignore) {
            try { raw = (String) user.getClass().getMethod("getAvatar").invoke(user); } catch (Exception ignored) {}
        }
        if (raw == null || raw.isBlank()) return null;

        String p = raw.trim().replace("\\", "/");

        // Lấy phần sau /uploads/ nếu lỡ lưu absolute
        int idx = p.indexOf("/uploads/");
        if (idx >= 0) p = p.substring(idx);

        // Ép prefix /uploads/
        if (!p.startsWith("/uploads/")) {
            if (p.startsWith("uploads/")) p = "/" + p;
            else p = "/uploads/" + p.replaceFirst("^/+", "");
        }

        // Sửa trùng thư mục
        p = p.replace("/avatars/avatars/", "/avatars/");

        // Đảm bảo file tồn tại ở ./uploads/**
        Path servedRoot = Paths.get("uploads");
        String rel = p.replaceFirst("^/uploads/", "");
        Path wanted = servedRoot.resolve(rel).normalize();

        if (!Files.exists(wanted)) {
            // Fallback 1: file hiện đang nằm sai thư mục avatars/avatars
            if (rel.startsWith("avatars/")) {
                String filename = rel.substring("avatars/".length());
                Path alt = servedRoot.resolve("avatars/avatars").resolve(filename).normalize();
                try {
                    if (Files.exists(alt)) {
                        Files.createDirectories(wanted.getParent());
                        Files.copy(alt, wanted, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ignored) {}
            }
            // Fallback 2: copy từ target/uploads
            if (!Files.exists(wanted)) {
                Path src = Paths.get("target", "uploads").resolve(rel).normalize();
                try {
                    if (Files.exists(src)) {
                        Files.createDirectories(wanted.getParent());
                        Files.copy(src, wanted, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ignored) {}
            }
        }
        return p; // luôn dạng /uploads/avatars/<file>
    }
}
