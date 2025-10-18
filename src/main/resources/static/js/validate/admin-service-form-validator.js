/**
 * Validator cho form tạo và chỉnh sửa dịch vụ (Admin Services)
 * Áp dụng cho: /admin/services/create, /admin/services/{id}/edit
 */
document.addEventListener('DOMContentLoaded', () => {
    // Tìm form tạo hoặc sửa dịch vụ
    const serviceForm = document.querySelector('form[th\\:action*="/admin/services"]')
                     || document.querySelector('form[action*="/admin/services"]');

    if (!serviceForm) {
        return; // Không có form services trên trang này
    }

    const validator = new FormValidator(serviceForm);

    // ========== name (Tên dịch vụ) ==========
    validator.addRule('name', 'required', 'Vui lòng nhập tên dịch vụ');
    validator.addRule('name', 'minLength:2', 'Tên dịch vụ phải có ít nhất 2 ký tự');
    validator.addRule('name', 'maxLength:200', 'Tên dịch vụ tối đa 200 ký tự');

    // ========== slug (Slug URL) ==========
    validator.addRule('slug', 'required', 'Vui lòng nhập slug');
    validator.addRule('slug', 'minLength:2', 'Slug phải có ít nhất 2 ký tự');
    validator.addRule('slug', 'maxLength:220', 'Slug tối đa 220 ký tự');
    validator.addRule('slug', 'pattern:^(?!-)(?!.*--)[a-z0-9-]+(?<!-)$',
        "Slug chỉ gồm chữ thường, số, dấu gạch ngang; không bắt đầu/kết thúc bằng '-' và không có '--'");

    // ========== unit (Đơn vị) - Optional ==========
    validator.addRule('unit', 'maxLength:50', 'Đơn vị tối đa 50 ký tự');
    validator.addRule('unit', 'pattern:^[A-Za-zÀ-ỹ0-9\\s\\/-]*$',
        "Đơn vị chỉ gồm chữ, số, khoảng trắng, '/' hoặc '-'");

    // ========== description (Mô tả) - Optional ==========
    validator.addRule('description', 'maxLength:1000', 'Mô tả tối đa 1000 ký tự');

    console.log('Admin Service Form Validator initialized');
});
