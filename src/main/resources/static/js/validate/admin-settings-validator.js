/**
 * Validator cho trang Admin Settings
 * Áp dụng cho: /admin/settings (tất cả 6 tabs)
 *
 * Tabs:
 * - core: Cấu hình tổng quan (siteName, timezone, currency)
 * - orders: Cấu hình đơn dịch vụ (minNoticeHours, maxJobsPerDay)
 * - payments: Cấu hình thanh toán (COD, VNPay, MoMo + vnpay.secret)
 * - upload: Cấu hình upload (maxSizeMb)
 * - review: Cấu hình đánh giá (minStarsVisible)
 * - security: Cấu hình bảo mật (maintenance checkbox - không cần validate)
 */
document.addEventListener('DOMContentLoaded', () => {

    // ========== TAB CORE ==========
    const coreForm = document.querySelector('form[action*="/admin/settings/save/core"]');
    if (coreForm) {
        const validator = new FormValidator(coreForm);

        // siteName - Tên hệ thống
        validator.addRule('siteName', 'required', 'Vui lòng nhập tên hệ thống');
        validator.addRule('siteName', 'minLength:2', 'Tên hệ thống phải có ít nhất 2 ký tự');
        validator.addRule('siteName', 'maxLength:100', 'Tên hệ thống tối đa 100 ký tự');

        // timezone - Múi giờ
        validator.addRule('timezone', 'required', 'Vui lòng nhập timezone');
        validator.addRule('timezone', 'pattern:^[A-Za-z_\\/+-]{3,50}$',
            "Timezone chỉ gồm ký tự chữ, '_', '/', '+', '-'");

        // currency - Mã tiền tệ
        validator.addRule('currency', 'required', 'Vui lòng nhập mã tiền tệ');
        validator.addRule('currency', 'pattern:^[A-Z]{3}$',
            'Mã tiền tệ phải gồm 3 ký tự in hoa (VD: VND, USD)');
    }

    // ========== TAB ORDERS ==========
    const ordersForm = document.querySelector('form[action*="/admin/settings/save/orders"]');
    if (ordersForm) {
        const validator = new FormValidator(ordersForm);

        // minNoticeHours - Báo trước tối thiểu (giờ)
        validator.addRule('minNoticeHours', 'required', 'Vui lòng nhập số giờ báo trước');
        validator.addRule('minNoticeHours', 'integer', 'Phải là số nguyên không âm');
        validator.addRule('minNoticeHours', 'maxValue:168', 'Tối đa 168 giờ (7 ngày)');

        // maxJobsPerDay - Số job tối đa/ngày
        validator.addRule('maxJobsPerDay', 'required', 'Vui lòng nhập số job tối đa/ngày');
        validator.addRule('maxJobsPerDay', 'integer', 'Phải là số nguyên dương');
        validator.addRule('maxJobsPerDay', 'minValue:1', 'Phải ít nhất 1 job/ngày');
        validator.addRule('maxJobsPerDay', 'maxValue:100', 'Không vượt quá 100');
    }

    // ========== TAB PAYMENTS ==========
    const paymentsForm = document.querySelector('form[action*="/admin/settings/save/payments"]');
    if (paymentsForm) {
        const validator = new FormValidator(paymentsForm);

        // Conditional validation cho vnpay.secret
        const vnpayCheckbox = paymentsForm.querySelector('input[type="checkbox"][name="vnpay.enabled"]');
        const vnpaySecretInput = paymentsForm.querySelector('input[name="vnpay.secret"]');

        if (vnpayCheckbox && vnpaySecretInput) {
            // Override validate method để thêm conditional logic
            const originalValidate = validator.validate.bind(validator);
            validator.validate = function() {
                // Clear previous rules for vnpay.secret
                if (this.fields['vnpay.secret']) {
                    this.fields['vnpay.secret'].rules = [];
                }

                // Add rules only if vnpay is enabled
                if (vnpayCheckbox.checked) {
                    this.addRule('vnpay.secret', 'required', 'Vui lòng nhập VNPay secret khi bật VNPay');
                    this.addRule('vnpay.secret', 'minLength:10', 'Secret tối thiểu 10 ký tự');
                    this.addRule('vnpay.secret', 'maxLength:200', 'Secret tối đa 200 ký tự');
                }

                return originalValidate();
            };

            // Also listen to checkbox change to clear error when unchecked
            vnpayCheckbox.addEventListener('change', () => {
                if (!vnpayCheckbox.checked) {
                    validator.clearError(vnpaySecretInput);
                }
            });
        }
    }

    // ========== TAB UPLOAD ==========
    const uploadForm = document.querySelector('form[action*="/admin/settings/save/upload"]');
    if (uploadForm) {
        const validator = new FormValidator(uploadForm);

        // maxSizeMb - Kích thước tối đa (MB)
        validator.addRule('maxSizeMb', 'required', 'Vui lòng nhập dung lượng tối đa (MB)');
        validator.addRule('maxSizeMb', 'integer', 'Phải là số nguyên dương');
        validator.addRule('maxSizeMb', 'minValue:1', 'Phải ít nhất 1 MB');
        validator.addRule('maxSizeMb', 'maxValue:1024', 'Không vượt quá 1024 MB (1GB)');
    }

    // ========== TAB REVIEW ==========
    const reviewForm = document.querySelector('form[action*="/admin/settings/save/review"]');
    if (reviewForm) {
        const validator = new FormValidator(reviewForm);

        // minStarsVisible - Ngưỡng sao tối thiểu hiển thị
        validator.addRule('minStarsVisible', 'required', 'Vui lòng nhập ngưỡng sao tối thiểu');
        validator.addRule('minStarsVisible', 'integer', 'Phải là số nguyên');
        validator.addRule('minStarsVisible', 'minValue:1', 'Giá trị phải từ 1 đến 5');
        validator.addRule('minStarsVisible', 'maxValue:5', 'Giá trị phải từ 1 đến 5');
    }

    // ========== TAB SECURITY ==========
    // Không cần validation (chỉ có checkbox maintenance)

    console.log('Admin Settings Validator initialized');
});
