document.addEventListener('DOMContentLoaded', () => {
    // Set max date for dob field to today
    const dobInput = document.getElementById('dob');
    if (dobInput) {
        const today = new Date().toISOString().split('T')[0];
        dobInput.setAttribute('max', today);
    }

    // Select the form using its action attribute
    const validator = new FormValidator('form[action="/register"]');

    // Add validation rules for User fields
    validator.addRule('username', 'required', 'Tên đăng nhập không được để trống.');
    validator.addRule('username', 'minLength:3', 'Tên đăng nhập phải có ít nhất 3 ký tự.');
    validator.addRule('username', 'maxLength:50', 'Tên đăng nhập không được quá 50 ký tự.');
    validator.addRule('username', 'alphanumeric', 'Tên đăng nhập chỉ được chứa chữ cái và số.');

    validator.addRule('password', 'required', 'Mật khẩu không được để trống.');
    validator.addRule('password', 'minLength:6', 'Mật khẩu phải có ít nhất 6 ký tự.');

    validator.addRule('passwordConfirm', 'required', 'Vui lòng xác nhận mật khẩu.');
    validator.addRule('passwordConfirm', 'matchField:password', 'Mật khẩu xác nhận không khớp.');

    validator.addRule('email', 'required', 'Email không được để trống.');
    validator.addRule('email', 'email', 'Email không hợp lệ.');

    validator.addRule('phone', 'phone', 'Số điện thoại không hợp lệ (phải có 10 chữ số).');

    // Add validation rules for Customer Profile fields (optional but recommended)
    validator.addRule('fullName', 'vietnameseFullName', 'Họ và tên chỉ được chứa chữ cái tiếng Việt, không có ký tự đặc biệt hoặc số.');
    validator.addRule('fullName', 'maxLength:100', 'Họ và tên không được quá 100 ký tự.');
    validator.addRule('addressLine', 'maxLength:255', 'Địa chỉ không được quá 255 ký tự.');

    // Add age validation for date of birth
    validator.addRule('dob', 'isOver18', 'Bạn phải đủ 18 tuổi để đăng ký.');
});
