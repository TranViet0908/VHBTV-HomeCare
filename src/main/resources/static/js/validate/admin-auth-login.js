document.addEventListener('DOMContentLoaded', () => {
    // Select the form using its action attribute
    const validator = new FormValidator('form[action="/admin/login"]');

    // Add validation rules
    validator.addRule('username', 'required', 'Tên đăng nhập không được để trống.');
    validator.addRule('password', 'required', 'Mật khẩu không được để trống.');
});
