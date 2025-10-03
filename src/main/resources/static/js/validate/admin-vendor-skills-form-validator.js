// Validator cho /admin/vendor-skills (create + edit)
// Ghi chú: Comment tiếng Việt để rõ ràng, dễ bảo trì.

document.addEventListener('DOMContentLoaded', () => {
  // Tìm form chính: có vendorId + name (cả create & edit đều như vậy)
  const forms = Array.from(document.querySelectorAll('form[method="post"]'));
  const form = forms.find(f =>
    f.querySelector('[name="vendorId"]') &&
    f.querySelector('[name="name"]')
  );
  if (!form) return;

  const fv = new FormValidator('form[method="post"]');
  if (!fv || !fv.form) return;

  // 1) Vendor: bắt buộc + phải là số nguyên không âm (id hợp lệ)
  fv.addRule('vendorId', 'required', 'Vui lòng chọn vendor');
  fv.addRule('vendorId', 'integer', 'Vendor không hợp lệ');
  fv.addRule('vendorId', 'minValue:0', 'Vendor không hợp lệ');

  // 2) Tên kỹ năng
  fv.addRule('name', 'required', 'Vui lòng nhập tên kỹ năng');
  fv.addRule('name', 'minLength:2', 'Tên kỹ năng phải có ít nhất 2 ký tự');
  fv.addRule('name', 'maxLength:150', 'Tên kỹ năng tối đa 150 ký tự');

  // 3) Slug (optional). Core.js sẽ bỏ qua nếu rỗng (không required)
  const slugPattern = '^(?!-)(?!.*--)[a-z0-9-]+(?<!-)$';
  if (form.querySelector('[name="slug"]')) {
    fv.addRule('slug', 'minLength:2', 'Slug phải có ít nhất 2 ký tự');
    fv.addRule('slug', 'maxLength:180', 'Slug tối đa 180 ký tự');
    fv.addRule('slug', `pattern:${slugPattern}`, 'Slug chỉ gồm chữ thường, số, gạch nối; không bắt đầu/kết thúc bằng “-” và không chứa “--”');
  }
});
