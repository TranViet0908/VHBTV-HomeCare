document.addEventListener('DOMContentLoaded', () => {
    const createSelector = 'form[action="/admin/users"]';
    const editSelector = 'form[action^="/admin/users/"]';
    const createForm = document.querySelector(createSelector);
    const editForm = document.querySelector(editSelector);
    const targetSelector = createForm ? createSelector : (editForm ? editSelector : null);

    if (!targetSelector) {
        console.warn('Khong tim thay form nguoi dung de gan validate.');
        return;
    }

    const validator = new FormValidator(targetSelector);
    const form = document.querySelector(targetSelector);
    const isCreate = !!createForm;

    // Username: bat buoc, 3-50 ky tu, chi gom chu va so
    validator.addRule('username', 'required', 'Vui long nhap ten dang nhap');
    validator.addRule('username', 'minLength:3', 'Ten dang nhap toi thieu 3 ky tu');
    validator.addRule('username', 'maxLength:50', 'Ten dang nhap toi da 50 ky tu');
    validator.addRule('username', 'alphanumeric', 'Ten dang nhap chi gom chu va so');

    // Email: bat buoc, dinh dang hop le
    validator.addRule('email', 'required', 'Vui long nhap email');
    validator.addRule('email', 'email', 'Email khong hop le');

    // So dien thoai: khong bat buoc, kiem tra khi co gia tri
    validator.addRule('phone', 'phone', 'So dien thoai khong hop le (10 so, bat dau bang 0)');

    if (isCreate) {
        // Tao moi: mat khau va xac nhan bat buoc
        validator.addRule('password', 'required', 'Vui long nhap mat khau');
        validator.addRule('password', 'minLength:6', 'Mat khau toi thieu 6 ky tu');
        validator.addRule('passwordConfirm', 'required', 'Vui long xac nhan mat khau');
        validator.addRule('passwordConfirm', 'matchField:password', 'Mat khau xac nhan khong khop');
    } else {
        // Chinh sua: chi kiem tra mat khau khi co nhap
        validator.addRule('password', 'passwordMinIfFilled', 'Mat khau moi toi thieu 6 ky tu');
        validator.addRule('passwordConfirm', 'passwordConfirmIfFilled', 'Vui long xac nhan mat khau moi va dam bao khop');
        validator.addRule('active', 'booleanSelect', 'Trang thai khong hop le');
    }

    // Vai tro: bat buoc chon
    validator.addRule('role', 'required', 'Vui long chon vai tro');

    // Avatar: kiem tra loai file va kich thuoc khi upload
    validator.addRule('avatar', 'avatarFile', 'Anh khong hop le (jpeg/png/webp, toi da 2MB)');

    const originalCheckRule = validator.checkRule.bind(validator);
    validator.checkRule = (element, rule) => {
        const value = element && typeof element.value === 'string' ? element.value.trim() : '';

        if (rule === 'passwordMinIfFilled') {
            if (value === '') return true;
            return value.length >= 6;
        }

        if (rule === 'passwordConfirmIfFilled') {
            const passwordElement = form.querySelector('[name="password"]');
            const passwordValue = passwordElement ? passwordElement.value.trim() : '';
            if (passwordValue === '' && value === '') return true;
            return value.length >= 6 && value === passwordValue;
        }

        if (rule === 'booleanSelect') {
            if (value === '') return true;
            return value === 'true' || value === 'false';
        }

        if (rule === 'avatarFile') {
            if (!element.files || element.files.length === 0) {
                return true;
            }
            const file = element.files[0];
            const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
            const maxSizeBytes = 2 * 1024 * 1024;
            return allowedTypes.includes(file.type) && file.size <= maxSizeBytes;
        }

        return originalCheckRule(element, rule);
    };
});
