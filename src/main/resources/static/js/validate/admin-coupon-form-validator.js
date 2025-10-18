/**
 * Validator cho form tạo mã giảm giá (Admin)
 * File: admin-coupon-form-validator.js
 * Sử dụng FormValidator framework từ core.js
 */

document.addEventListener('DOMContentLoaded', () => {
    const validator = new FormValidator('couponForm');

    // ====== VALIDATION CƠ BẢN ======

    // Code: bắt buộc, 5-20 ký tự, chỉ chữ IN HOA và số
    validator.addRule('code', 'required', 'Vui lòng nhập mã giảm giá.');
    validator.addRule('code', 'minLength:5', 'Mã giảm giá phải có ít nhất 5 ký tự.');
    validator.addRule('code', 'maxLength:20', 'Mã giảm giá không được vượt quá 20 ký tự.');

    // Name: bắt buộc, tối đa 100 ký tự
    validator.addRule('name', 'required', 'Vui lòng nhập tên hiển thị.');
    validator.addRule('name', 'maxLength:100', 'Tên hiển thị không được vượt quá 100 ký tự.');

    // Value: bắt buộc, phải là số > 0
    validator.addRule('value', 'required', 'Vui lòng nhập giá trị giảm giá.');

    // ====== CUSTOM VALIDATION PHỨC TẠP ======

    // Thêm custom rule: code chỉ chấp nhận chữ IN HOA và số
    const codeField = validator.fields['code'];
    if (codeField) {
        codeField.rules.push({
            rule: 'customCodeFormat',
            message: 'Mã giảm giá chỉ được chứa chữ cái IN HOA (A-Z) và số (0-9).'
        });
    }

    // Thêm custom rule: value phải là số > 0
    const valueField = validator.fields['value'];
    if (valueField) {
        valueField.rules.push({
            rule: 'customValuePositive',
            message: 'Giá trị giảm giá phải là số lớn hơn 0.'
        });
        valueField.rules.push({
            rule: 'customValueRange',
            message: 'Nếu loại giảm giá là "Phần trăm", giá trị phải trong khoảng từ 0 đến 100.'
        });
    }

    // Thêm custom rule: maxDiscountAmount phải >= 0 (nếu có nhập)
    const maxDiscountField = validator.fields['maxDiscountAmount'];
    if (!maxDiscountField) {
        // Nếu field chưa được thêm rule, tạo mới
        const element = validator.form.querySelector('[name="maxDiscountAmount"]');
        if (element) {
            validator.fields['maxDiscountAmount'] = {
                rules: [],
                element: element,
                errorElement: null
            };
            element.addEventListener('blur', () => {
                validator.validateField('maxDiscountAmount');
            });
        }
    }
    if (validator.fields['maxDiscountAmount']) {
        validator.fields['maxDiscountAmount'].rules.push({
            rule: 'customNonNegativeNumber',
            message: 'Giới hạn giảm tối đa phải là số không âm (>= 0).'
        });
    }

    // Thêm custom rule: usageLimitGlobal phải là số nguyên >= 0 (nếu có nhập)
    const usageLimitGlobalField = validator.fields['usageLimitGlobal'];
    if (!usageLimitGlobalField) {
        const element = validator.form.querySelector('[name="usageLimitGlobal"]');
        if (element) {
            validator.fields['usageLimitGlobal'] = {
                rules: [],
                element: element,
                errorElement: null
            };
            element.addEventListener('blur', () => {
                validator.validateField('usageLimitGlobal');
            });
        }
    }
    if (validator.fields['usageLimitGlobal']) {
        validator.fields['usageLimitGlobal'].rules.push({
            rule: 'customNonNegativeInteger',
            message: 'Giới hạn toàn hệ thống phải là số nguyên không âm (>= 0).'
        });
    }

    // Thêm custom rule: usageLimitPerUser phải là số nguyên >= 0 (nếu có nhập)
    const usageLimitPerUserField = validator.fields['usageLimitPerUser'];
    if (!usageLimitPerUserField) {
        const element = validator.form.querySelector('[name="usageLimitPerUser"]');
        if (element) {
            validator.fields['usageLimitPerUser'] = {
                rules: [],
                element: element,
                errorElement: null
            };
            element.addEventListener('blur', () => {
                validator.validateField('usageLimitPerUser');
            });
        }
    }
    if (validator.fields['usageLimitPerUser']) {
        validator.fields['usageLimitPerUser'].rules.push({
            rule: 'customNonNegativeInteger',
            message: 'Giới hạn mỗi người dùng phải là số nguyên không âm (>= 0).'
        });
    }

    // Thêm custom rule: kiểm tra endAt phải sau startAt
    const startAtField = validator.form.querySelector('[name="startAt"]');
    const endAtField = validator.form.querySelector('[name="endAt"]');
    if (startAtField && endAtField) {
        if (!validator.fields['endAt']) {
            validator.fields['endAt'] = {
                rules: [],
                element: endAtField,
                errorElement: null
            };
            endAtField.addEventListener('blur', () => {
                validator.validateField('endAt');
            });
        }
        validator.fields['endAt'].rules.push({
            rule: 'customDateRange',
            message: 'Ngày kết thúc phải sau ngày bắt đầu.'
        });
    }

    // ====== MỞ RỘNG checkRule CỦA FORMVALIDATOR ======

    const originalCheckRule = validator.checkRule.bind(validator);
    validator.checkRule = function(element, rule) {
        const value = element.value.trim();

        // Custom rule: code chỉ chấp nhận chữ IN HOA và số
        if (rule === 'customCodeFormat') {
            if (value === '') return true; // Skip nếu rỗng (required sẽ xử lý)
            const codeRegex = /^[A-Z0-9]+$/;
            return codeRegex.test(value);
        }

        // Custom rule: value phải là số > 0
        if (rule === 'customValuePositive') {
            if (value === '') return true;
            const numValue = parseFloat(value);
            return !isNaN(numValue) && numValue > 0;
        }

        // Custom rule: nếu type = PERCENT thì value phải 0 < value <= 100
        if (rule === 'customValueRange') {
            if (value === '') return true;
            const typeElement = validator.form.querySelector('[name="type"]');
            if (!typeElement) return true;

            const type = typeElement.value;
            if (type === 'PERCENT') {
                const numValue = parseFloat(value);
                if (isNaN(numValue)) return true; // Lỗi số sẽ được xử lý bởi customValuePositive
                return numValue > 0 && numValue <= 100;
            }
            return true;
        }

        // Custom rule: số không âm (>= 0)
        if (rule === 'customNonNegativeNumber') {
            if (value === '') return true; // Không bắt buộc, cho phép để trống
            const numValue = parseFloat(value);
            return !isNaN(numValue) && numValue >= 0;
        }

        // Custom rule: số nguyên không âm (>= 0)
        if (rule === 'customNonNegativeInteger') {
            if (value === '') return true; // Không bắt buộc
            const numValue = parseFloat(value);
            return !isNaN(numValue) && numValue >= 0 && Number.isInteger(numValue);
        }

        // Custom rule: endAt phải sau startAt
        if (rule === 'customDateRange') {
            const startAtElement = validator.form.querySelector('[name="startAt"]');
            if (!startAtElement) return true;

            const startAtValue = startAtElement.value.trim();
            const endAtValue = value;

            // Chỉ validate nếu cả 2 trường đều có giá trị
            if (startAtValue === '' || endAtValue === '') return true;

            const startDate = new Date(startAtValue);
            const endDate = new Date(endAtValue);

            return endDate > startDate;
        }

        // Gọi lại hàm checkRule gốc cho các rule chuẩn
        return originalCheckRule(element, rule);
    };

    console.log('Admin Coupon Form Validator đã được khởi tạo.');
});
