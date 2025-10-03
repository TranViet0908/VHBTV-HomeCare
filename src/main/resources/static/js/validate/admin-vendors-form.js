// Validator cho form Chinh sua Vendor (admin/vendors/edit.html)
// Giai thich: File nay su dung FormValidator (core.js) de kiem tra du lieu truoc khi submit
// - Viet comment bang tieng Viet de de doc va bao tri

document.addEventListener('DOMContentLoaded', () => {
    // Xac dinh form muc tieu: duong dan /admin/vendors/{id}/update
    const formSelector = 'form[action^="/admin/vendors/"][action$="/update"]';
    const form = document.querySelector(formSelector);
    if (!form) {
        console.warn('Khong tim thay form vendor de gan validate.');
        return;
    }

    // Khoi tao Validator
    const validator = new FormValidator(formSelector);

    // 1) displayName: bat buoc, 2-100 ky tu
    // Ly do: ten hien thi dung cho UI/SEO, can co va do dai hop ly
    validator.addRule('displayName', 'required', 'Vui long nhap ten hien thi');
    validator.addRule('displayName', 'minLength:2', 'Ten hien thi toi thieu 2 ky tu');
    validator.addRule('displayName', 'maxLength:100', 'Ten hien thi toi da 100 ky tu');

    // 2) legalName: khong bat buoc, neu nhap thi toi da 150 ky tu
    // Ly do: ten phap ly co the khong co doi voi ca nhan; gioi han do dai de tranh nhap qua dai
    validator.addRule('legalName', 'maxLength:150', 'Ten phap ly toi da 150 ky tu');

    // 3) yearsExperience: so nguyen >= 0, gioi han thuc te <= 80 nam
    // Ly do: tranh gia tri am/khong hop ly; 0..80 phu hop thuc te
    validator.addRule('yearsExperience', 'integer', 'Kinh nghiem phai la so nguyen khong am');
    validator.addRule('yearsExperience', 'minValue:0', 'Kinh nghiem khong duoc nho hon 0');
    validator.addRule('yearsExperience', 'maxValue:80', 'Kinh nghiem khong duoc vuot qua 80 nam');

    // 4) addressLine: khong bat buoc, toi da 200 ky tu
    // Ly do: dia chi thuong dai, nhung can gioi han cho dep UI
    validator.addRule('addressLine', 'maxLength:200', 'Dia chi toi da 200 ky tu');

    // 5) bio: khong bat buoc, toi da 1000 ky tu
    // Ly do: gioi thieu dai hon nhung can gioi han de tranh spam
    validator.addRule('bio', 'maxLength:1000', 'Gioi thieu toi da 1000 ky tu');
});

