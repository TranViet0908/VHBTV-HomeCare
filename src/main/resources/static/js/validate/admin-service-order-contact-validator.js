/**
 * Trinh xac thuc cho form cap nhat thong tin lien he don dich vu.
 * Su dung lai FormValidator trong core.js.
 */
document.addEventListener('DOMContentLoaded', () => {
    const formId = 'update-contact-form';
    const formElement = document.getElementById(formId);

    // Neu form khong ton tai thi bo qua de tranh loi tren cac trang khac.
    if (!formElement) {
        console.warn('Khong tim thay form cap nhat lien he de gan validate.');
        return;
    }

    const validator = new FormValidator(formId);

    // Ten lien he: bat buoc, phai la ten tieng Viet hop le, toi da 100 ky tu.
    validator.addRule('contactName', 'required', 'Vui long nhap ten nguoi nhan dich vu.');
    validator.addRule('contactName', 'vietnameseFullName', 'Ten nguoi nhan chi nen chua chu cai tieng Viet va khoang trang.');
    validator.addRule('contactName', 'maxLength:100', 'Ten nguoi nhan khong duoc vuot qua 100 ky tu.');

    // So dien thoai: bat buoc, dinh dang 10 so va bat dau bang 0.
    validator.addRule('contactPhone', 'required', 'Vui long nhap so dien thoai lien he.');
    validator.addRule('contactPhone', 'phone', 'So dien thoai phai gom 10 so va bat dau bang so 0.');

    // Dia chi: bat buoc, toi da 255 ky tu.
    validator.addRule('addressLine', 'required', 'Vui long nhap dia chi lien he.');
    validator.addRule('addressLine', 'maxLength:255', 'Dia chi khong duoc vuot qua 255 ky tu.');

    // Ghi chu: khong bat buoc nhung gioi han 500 ky tu neu nhap.
    validator.addRule('notes', 'maxLength:500', 'Ghi chu khong duoc vuot qua 500 ky tu.');
});
