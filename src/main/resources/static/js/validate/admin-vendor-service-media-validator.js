document.addEventListener('DOMContentLoaded', () => {

    // ========== FORM 1: UPLOAD ẢNH (#imgForm) ==========
    const imgForm = document.getElementById('imgForm');
    if (imgForm) {
        const validator = new FormValidator(imgForm);

        // vsId validation (có name, dùng core validator)
        validator.addRule('vsId', 'required', 'Vui lòng chọn dịch vụ');

        // Custom validation cho vendor1 (không có name) và files
        imgForm.addEventListener('submit', (e) => {
            let hasError = false;

            // Validate vendor1 (select không có name)
            const vendor1 = document.getElementById('vendor1');
            if (!vendor1.value) {
                validator.showError(vendor1, 'Vui lòng chọn vendor');
                hasError = true;
            }

            // Validate files
            const filesInput = document.getElementById('filesInput1');
            const fileError = validateImageFiles(filesInput);
            if (fileError) {
                validator.showError(filesInput, fileError);
                hasError = true;
            }

            if (hasError) {
                e.preventDefault();
            }
        });

        // Clear error khi thay đổi vendor
        document.getElementById('vendor1').addEventListener('change', function() {
            validator.clearError(this);
        });

        // Clear error khi thay đổi files
        document.getElementById('filesInput1').addEventListener('change', function() {
            validator.clearError(this);
        });
    }

    // ========== FORM 2: THÊM VIDEO (#vidForm) ==========
    const vidForm = document.getElementById('vidForm');
    if (vidForm) {
        const validator = new FormValidator(vidForm);

        // vsId validation
        validator.addRule('vsId', 'required', 'Vui lòng chọn dịch vụ');

        // url validation
        validator.addRule('url', 'required', 'Vui lòng nhập URL video');
        validator.addRule('url', 'pattern:^https?:\\/\\/[^\\s]+\\.(mp4|webm)(\\?.*)?$',
            'URL phải là http(s) và kết thúc bằng .mp4 hoặc .webm');

        // altText validation (optional)
        validator.addRule('altText', 'maxLength:255', 'Mô tả (alt) tối đa 255 ký tự');

        // sortOrder validation (optional)
        validator.addRule('sortOrder', 'integer', 'Thứ tự phải là số nguyên không âm');
        validator.addRule('sortOrder', 'minValue:0', 'Thứ tự phải >= 0');

        // Custom validation cho vendor2
        vidForm.addEventListener('submit', (e) => {
            const vendor2 = document.getElementById('vendor2');
            if (!vendor2.value) {
                validator.showError(vendor2, 'Vui lòng chọn vendor');
                e.preventDefault();
            }
        });

        // Clear error khi thay đổi vendor
        document.getElementById('vendor2').addEventListener('change', function() {
            validator.clearError(this);
        });
    }

    // ========== FORM 3: EDIT MEDIA (edit.html) ==========
    const editForm = document.querySelector('form[action*="/admin/vendor-service-media/"][action*="/update"]');
    if (editForm) {
        const validator = new FormValidator(editForm);

        // Conditional validation cho url (chỉ khi không disabled)
        const urlInput = editForm.querySelector('input[name="url"]');
        if (urlInput && !urlInput.disabled) {
            validator.addRule('url', 'required', 'Vui lòng nhập URL video');
            validator.addRule('url', 'pattern:^https?:\\/\\/[^\\s]+\\.(mp4|webm)(\\?.*)?$',
                'URL phải là http(s) và kết thúc bằng .mp4 hoặc .webm');
        }

        // altText validation (optional)
        validator.addRule('altText', 'maxLength:255', 'Mô tả (alt) tối đa 255 ký tự');

        // sortOrder validation (optional)
        validator.addRule('sortOrder', 'integer', 'Thứ tự phải là số nguyên không âm');
        validator.addRule('sortOrder', 'minValue:0', 'Thứ tự phải >= 0');
    }

    // ========== HELPER: Validate Image Files ==========
    function validateImageFiles(inputElement) {
        const files = inputElement.files;

        // Check required
        if (!files || files.length === 0) {
            return 'Vui lòng chọn ít nhất 1 ảnh';
        }

        // Check max files
        if (files.length > 10) {
            return 'Bạn chỉ được chọn tối đa 10 ảnh';
        }

        // Check file type and size
        const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
        const allowedExtensions = ['.jpg', '.jpeg', '.png', '.webp'];
        const maxSizeBytes = 5 * 1024 * 1024; // 5MB

        for (let i = 0; i < files.length; i++) {
            const file = files[i];

            // Check MIME type or extension
            const isValidType = allowedTypes.includes(file.type) ||
                allowedExtensions.some(ext => file.name.toLowerCase().endsWith(ext));

            if (!isValidType) {
                return 'Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP';
            }

            // Check file size
            if (file.size > maxSizeBytes) {
                return `Ảnh "${file.name}" vượt quá 5MB`;
            }
        }

        return null; // No error
    }
});
