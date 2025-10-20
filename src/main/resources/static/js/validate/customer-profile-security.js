document.addEventListener('DOMContentLoaded', () => {
    // Select the security form using action attribute
    const validator = new FormValidator('form[action*="/security"]');

    // Current password validation
    validator.addRule('current_password', 'required', 'Mật khẩu hiện tại không được để trống.');
    validator.addRule('current_password', 'minLength:6', 'Mật khẩu phải có ít nhất 6 ký tự.');

    // New password validation - strong password requirements
    validator.addRule('new_password', 'required', 'Mật khẩu mới không được để trống.');
    validator.addRule('new_password', 'minLength:8', 'Mật khẩu mới phải có ít nhất 8 ký tự.');
    validator.addRule('new_password', 'maxLength:20', 'Mật khẩu mới không được quá 20 ký tự.');
    validator.addRule('new_password', 'pattern:^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$',
        'Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&).');

    // Confirm password validation
    validator.addRule('confirm_password', 'required', 'Vui lòng xác nhận mật khẩu mới.');
    validator.addRule('confirm_password', 'matchField:new_password', 'Mật khẩu xác nhận không khớp với mật khẩu mới.');
});
