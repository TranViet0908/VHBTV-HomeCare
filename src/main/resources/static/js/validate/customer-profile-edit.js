document.addEventListener('DOMContentLoaded', () => {
    // Select the profile form
    const validator = new FormValidator('#profileForm');

    // Full name validation - optional, but if provided must be valid
    // validator.addRule('full_name', 'required', 'Họ và tên không được để trống.');
    validator.addRule('full_name', 'vietnameseFullName', 'Họ và tên chỉ được chứa chữ cái tiếng Việt và khoảng trống.');
    validator.addRule('full_name', 'minLength:2', 'Họ và tên phải có ít nhất 2 ký tự.');
    validator.addRule('full_name', 'maxLength:100', 'Họ và tên không được quá 100 ký tự.');

    // Email validation - optional at client side (server will check uniqueness)
    // validator.addRule('email', 'required', 'Email không được để trống.');
    validator.addRule('email', 'email', 'Email không hợp lệ (ví dụ: ten@example.com).');

    // Phone validation (Vietnamese format) - optional
    // validator.addRule('phone', 'required', 'Số điện thoại không được để trống.');
    validator.addRule('phone', 'phone', 'Số điện thoại phải là 10 chữ số, bắt đầu bằng 0.');

    // Date of birth validation - optional
    validator.addRule('dob', 'isOver18', 'Bạn phải đủ 18 tuổi để sử dụng dịch vụ.');

    // Address validation (optional, but if provided, check length)
    validator.addRule('address_line', 'maxLength:500', 'Địa chỉ không được quá 500 ký tự.');

    // Avatar file validation (custom handling)
    // Note: avatar input is outside form, use form="profileForm" attribute
    const avatarInput = document.getElementById('avatarInput');
    if (avatarInput) {
        avatarInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                // Check file type
                if (!file.type.startsWith('image/')) {
                    alert('Vui lòng chọn file ảnh (JPG, PNG, GIF).');
                    e.target.value = '';
                    return;
                }
                // Check file size (max 2MB)
                if (file.size > 2 * 1024 * 1024) {
                    alert('Kích thước ảnh không được vượt quá 2MB.');
                    e.target.value = '';
                    return;
                }
            }
        });
    }
});
