
class FormValidator {
    /**
     * Khởi tạo FormValidator.
     * @param {string|HTMLElement} formOrSelector - Bộ chọn CSS, ID của form, hoặc phần tử form.
     */
    constructor(formOrSelector) {
        if (typeof formOrSelector === 'string') {
            this.form = document.querySelector(formOrSelector) || document.getElementById(formOrSelector);
        } else if (formOrSelector instanceof HTMLElement) {
            this.form = formOrSelector;
        } else {
            this.form = null;
        }

        this.fields = {}; // { fieldName: { rules: [], element: null, errorElement: null } }

        if (!this.form) {
            console.error('Form not found. Please provide a valid selector or HTMLFormElement.');
            return;
        }

        this.form.addEventListener('submit', (e) => {
            if (!this.validate()) {
                e.preventDefault(); // Prevent submission if validation fails
                console.log('Validation failed. Form submission prevented.');
            }
        });

        // Listen for input events to clear errors dynamically
        this.form.addEventListener('input', (e) => {
            if (e.target.name && this.fields[e.target.name]) {
                this.clearError(e.target);
            }
        });
    }

    /**
     * Adds a validation rule for a specific field.
     * Thêm một quy tắc xác thực cho một trường cụ thể.
     * @param {string} fieldName - The name attribute of the input field.
     * @param {string} rule - The validation rule (e.g., 'required').
     * @param {string} message - The error message to display.
     */
    addRule(fieldName, rule, message) {
        if (!this.fields[fieldName]) {
            const element = this.form.querySelector(`[name="${fieldName}"]`);
            if (!element) {
                console.warn(`Field "${fieldName}" not found in the form.`);
                return;
            }
            this.fields[fieldName] = {
                rules: [],
                element: element,
                errorElement: null
            };

            // Add blur event listener for real-time validation
            element.addEventListener('blur', () => {
                this.validateField(fieldName);
            });
        }
        this.fields[fieldName].rules.push({ rule, message });
    }

    /**
     * Validates a single field.
     * Xác thực một trường duy nhất.
     * @param {string} fieldName - The name of the field to validate.
     */
    validateField(fieldName) {
        this.validate([fieldName]);
    }

    /**
     * Validates all fields in the form.
     * Xác thực tất cả các trường trong form.
     * @returns {boolean} - True if all fields are valid, false otherwise.
     */
    validate() {
        this.clearAllErrors();
        const fieldNames = Object.keys(this.fields);
        let isFormValid = true;

        for (const fieldName of fieldNames) {
            const field = this.fields[fieldName];
            this.clearError(field.element); // Clear previous error for the field

            for (const { rule, message } of field.rules) {
                if (!this.checkRule(field.element, rule)) {
                    this.showError(field.element, message);
                    isFormValid = false;
                    break; // Stop at the first error for this field
                }
            }
        }
        return isFormValid;
    }

    /**
     * Checks a single validation rule for an element.
     * Kiểm tra một quy tắc xác thực cho một phần tử.
     * @param {HTMLElement} element - The input element.
     * @param {string} rule - The validation rule to check.
     * @returns {boolean} - True if the rule passes, false otherwise.
     */
    checkRule(element, rule) {
        const value = element.value.trim();

        // Required rule
        if (rule === 'required') {
            return value !== '';
        }

        // Skip validation if field is empty and not required
        if (value === '' && rule !== 'required') {
            return true;
        }

        // Email validation
        if (rule === 'email') {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return emailRegex.test(value);
        }

        // Phone validation (Vietnamese format: 10 digits, starts with 0)
        if (rule === 'phone') {
            const phoneRegex = /^0\d{9}$/;
            return phoneRegex.test(value);
        }

        // Alphanumeric validation
        if (rule === 'alphanumeric') {
            const alphanumericRegex = /^[a-zA-Z0-9]+$/;
            return alphanumericRegex.test(value);
        }

        // Vietnamese full name validation - Chỉ cho phép chữ cái tiếng Việt có dấu và khoảng trắng
        if (rule === 'vietnameseFullName') {
            // Cho phép: chữ cái a-zA-Z, chữ tiếng Việt có dấu, khoảng trắng
            // Không cho phép: số, ký tự đặc biệt
            const vietnameseNameRegex = /^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ\s]+$/;
            return vietnameseNameRegex.test(value);
        }

        // Min length validation
        if (rule.startsWith('minLength:')) {
            const minLength = parseInt(rule.split(':')[1]);
            return value.length >= minLength;
        }

        // Max length validation
        if (rule.startsWith('maxLength:')) {
            const maxLength = parseInt(rule.split(':')[1]);
            return value.length <= maxLength;
        }

        // Match field validation (for password confirmation)
        if (rule.startsWith('matchField:')) {
            const fieldName = rule.split(':')[1];
            const matchElement = this.form.querySelector(`[name="${fieldName}"]`);
            if (!matchElement) {
                console.warn(`Field "${fieldName}" not found for matchField validation.`);
                return false;
            }
            return value === matchElement.value.trim();
        }

        // Age validation (must be 18 or older)
        if (rule === 'isOver18') {
            if (!value) return true; // Skip if empty (use 'required' rule separately)
            const birthDate = new Date(value);
            const today = new Date();
            let age = today.getFullYear() - birthDate.getFullYear();
            const monthDiff = today.getMonth() - birthDate.getMonth();
            if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
                age--;
            }
            return age >= 18;
        }

        // Pattern validation (custom regex)
        if (rule.startsWith('pattern:')) {
            const pattern = rule.substring(8); // Get regex pattern after 'pattern:'
            try {
                const regex = new RegExp(pattern);
                return regex.test(value);
            } catch (e) {
                console.error(`Invalid regex pattern: ${pattern}`, e);
                return false;
            }
        }

        // Integer validation (non-negative integer)
        if (rule === 'integer') {
            const num = Number(value);
            return Number.isInteger(num) && num >= 0;
        }

        // Minimum value validation
        if (rule.startsWith('minValue:')) {
            const minVal = parseFloat(rule.split(':')[1]);
            const num = parseFloat(value);
            return !isNaN(num) && num >= minVal;
        }

        // Maximum value validation
        if (rule.startsWith('maxValue:')) {
            const maxVal = parseFloat(rule.split(':')[1]);
            const num = parseFloat(value);
            return !isNaN(num) && num <= maxVal;
        }

        return true;
    }

    /**
     * Displays an error message for a specific field.
     * Hiển thị thông báo lỗi cho một trường cụ thể.
     * @param {HTMLElement} element - The input element.
     * @param {string} message - The error message.
     */
    showError(element, message) {
        let errorElement = element.nextElementSibling;
        if (!errorElement || !errorElement.classList.contains('validation-error')) {
            errorElement = document.createElement('div');
            errorElement.className = 'validation-error text-red-500 text-sm mt-1';
            element.parentNode.insertBefore(errorElement, element.nextSibling);
        }
        element.classList.add('border-red-500');
        errorElement.textContent = message;
    }

    /**
     * Clears an error message for a specific field.
     * Xóa thông báo lỗi cho một trường cụ thể.
     * @param {HTMLElement} element - The input element.
     */
    clearError(element) {
        element.classList.remove('border-red-500');
        const errorElement = element.nextElementSibling;
        if (errorElement && errorElement.classList.contains('validation-error')) {
            errorElement.remove();
        }
    }

    /**
     * Clears all validation errors from the form.
     * Xóa tất cả các lỗi xác thực khỏi form.
     */
    clearAllErrors() {
        for (const fieldName in this.fields) {
            const field = this.fields[fieldName];
            this.clearError(field.element);
        }
    }
}
