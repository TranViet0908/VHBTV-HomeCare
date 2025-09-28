# VHBTV Homecare — Sàn dịch vụ chăm sóc nhà cửa

> Kết nối **Customer** ↔ **Vendor** để thuê dịch vụ vệ sinh, sửa chữa, bảo trì nhà cửa. Frontend ưu tiên **Tailwind CDN** + **Thymeleaf**. Backend **Spring Boot** theo mô hình `Entity → Repository → Service → Controller`, chia theo vai trò **Admin / Customer / Vendor** dưới package gốc `Project.HouseService`.

---

## 1) Tính năng chính

### 1.1. Tài khoản & phân quyền
- Đăng nhập, hiển thị tên người dùng, menu tài khoản, đăng xuất từ header.  
- Vai trò: `ROLE_ADMIN`, `ROLE_CUSTOMER`, `ROLE_VENDOR`.  
- Thông tin người dùng gồm: username, email, phone, avatar_url, is_active.

### 1.2. Hồ sơ Vendor
- Hồ sơ vendor, kỹ năng vendor, danh mục dịch vụ chuẩn.  
- Duyệt hồ sơ ứng tuyển trở thành vendor. Khi duyệt **APPROVED** thì tạo/cập nhật vendor_profile và đổi vai trò user thành `ROLE_VENDOR`.

### 1.3. Gói dịch vụ của Vendor
- CRUD **Vendor Service**: tiêu đề, mô tả, đơn giá, đơn vị, thời lượng, trạng thái `ACTIVE|HIDDEN|PAUSED`.  
- Lọc theo vendor, dịch vụ, trạng thái, khoảng giá; tìm kiếm theo từ khóa; phân trang; xem chi tiết; đổi trạng thái; xoá.  
- Thư viện media theo gói: upload ảnh/video, chọn ảnh **cover**; cover được lưu ở trường `cover_url` của `vendor_service`.

### 1.4. Đơn dịch vụ & Thanh toán
- Tạo **Service Order** gồm nhiều **Service Order Item** của **cùng một vendor**.  
- Áp dụng **coupon** cho dịch vụ; ghi nhận **payment** theo đơn.  
- Theo dõi trạng thái đơn và kiểm tra ràng buộc số lượng, đơn giá ≥ 0.

### 1.5. Đánh giá & Chatbot
- Đánh giá **vendor** và **vendor_service** sau khi sử dụng.  
- Lưu lịch sử hội thoại: `chat_conversation` ↔ `chat_message`.

### 1.6. Khu vực Admin
- Sidebar tổng hợp: Users, Vendor Applications, Vendors, Vendor Skills, Vendor Services, Vendor Service Media, Services, Coupons, Payments, Reviews, Chatbot Logs, Reports, Settings.  
- Header/Footer dùng Tailwind CDN + Font Awesome.  
- Phân mảnh giao diện với `th:replace` để tái sử dụng layout.

---

## 2) Kiến trúc & luồng dữ liệu

### 2.1. Phân lớp backend
- **Entity**: ánh xạ bảng CSDL.  
- **Repository**: JPA thao tác dữ liệu.  
- **Service**: nghiệp vụ, validation, giao dịch.  
- **Controller**: tách theo vai trò `Admin/Customer/Vendor`.

### 2.2. Luồng nghiệp vụ tiêu biểu

#### A) Đăng ký làm Vendor
1. User gửi `vendor_application` trạng thái `PENDING`.  
2. Admin duyệt:  
   - `APPROVED`: tạo/cập nhật `vendor_profile`, đổi role user → `ROLE_VENDOR`.  
   - `REJECTED`: giữ nguyên, có thể nộp lại.

#### B) Tạo đơn dịch vụ
1. Customer chọn nhiều `vendor_service` **thuộc cùng vendor**.  
2. Tạo `service_order` + các `service_order_item` (thời gian thực hiện, giá, số lượng).  
3. Áp dụng `coupon` loại dịch vụ nếu hợp lệ.  
4. Tạo `payment` cho đơn, cập nhật trạng thái thanh toán.

#### C) Quản lý media của Vendor Service
1. Upload file vào thư viện của `vendor_service`.  
2. Chọn **Đặt cover** → cập nhật `vendor_service.cover_url`.  
3. Hiển thị cover trong danh sách và trang chi tiết gói.

---

## 3) Cơ sở dữ liệu

### 3.1. Bảng và quan hệ chính
user 1—1 customer_profile
user 1—1 vendor_profile 1—* vendor_skill
user 1—* vendor_application

service (parent_id) 1—* vendor_service
vendor_profile 1—* vendor_service

service_order 1—* service_order_item -* vendor_service

coupon --< coupon_user
coupon --< coupon_service -> vendor_service
coupon --< coupon_redemption (log)

vendor_profile <-* vendor_review
vendor_service <-* vendor_service_review

chat_conversation 1—* chat_message

markdown
Copy code

### 3.2. Ý nghĩa bảng
- **user**: tài khoản, vai trò, avatar, kích hoạt.  
- **customer_profile / vendor_profile**: thông tin hồ sơ.  
- **vendor_skill**: kỹ năng chuyên môn.  
- **vendor_application**: hồ sơ ứng tuyển `PENDING|APPROVED|REJECTED`.  
- **service**: danh mục dịch vụ gốc, có phân cấp `parent_id`.  
- **vendor_service**: gói dịch vụ của vendor, **cover_url**, giá, đơn vị, thời lượng, trạng thái.  
- **service_order / service_order_item**: đơn và hạng mục đặt lịch.  
- **coupon / coupon_user / coupon_service / coupon_redemption**: mã giảm giá và lịch sử dùng.  
- **payment**: thông tin thanh toán theo đơn.  
- **vendor_review / vendor_service_review**: đánh giá.  
- **chat_conversation / chat_message**: nhật ký hội thoại.

### 3.3. Ràng buộc — chỉ mục — kiểm tra
- **UNIQUE**: username, email, service.slug, coupon.code.  
- **CHECK**: số lượng > 0, đơn giá ≥ 0, rating 1..5.  
- **FK**: đảm bảo liên kết vendor/service/order.  
- **Index**: trường tìm kiếm như status, vendor_id, service_id, …  
- Trigger/logic đảm bảo nhất quán tổng tiền giữa order và items.

### 3.4. Dữ liệu mẫu đáng chú ý
- Seed user nhiều vai trò, có admin và customer `TranViet0908`.  
- Seed vendor_profile, vendor_skill, vendor_service với **cover_url** mẫu `/uploads/vendor-services/...`.  
- Seed service_order, service_order_item và payment cho cả product/service để đối chiếu báo cáo.

---

## 4) Giao diện quản trị

- **Header**: chào user, dropdown tài khoản, nút **Đăng xuất** (`POST /admin/logout`).  
- **Footer**: bản quyền, links chính sách, in ra ngày cập nhật bằng JS.  
- **Sidebar**: nhóm menu nghiệp vụ; auto highlight theo URL; có nhóm **Đánh giá** và **Báo cáo**.  
- **Vendor Services List**: bộ lọc từ khóa, vendor, dịch vụ, trạng thái, giá min/max; bảng dữ liệu; phân trang; hành động Chi tiết/Sửa/Đổi trạng thái/Xóa.

---

## 5) Cấu hình & chạy dự án

### 5.1. Yêu cầu
- JDK 21+, Maven 3.9+, MySQL 8.0+.  
- Tailwind dùng qua CDN trong template.

### 5.2. Khởi tạo CSDL
1. Tạo database `house_service` (UTF8MB4).  
2. Import file `house_service.sql` (và `house_service_user.sql` nếu tách bảng user).  

### 5.3. Cấu hình kết nối
Thêm vào `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/house_service?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Bangkok&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=********
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.thymeleaf.cache=false