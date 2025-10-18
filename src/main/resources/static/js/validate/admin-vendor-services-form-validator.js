// Validator cho /admin/vendor-services (create + edit)
// Chú thích tiếng Việt để dễ bảo trì

document.addEventListener('DOMContentLoaded', () => {
  // Tìm form vendor-services theo sự hiện diện các field chính
  const forms = Array.from(document.querySelectorAll('form[method="post"]'));
  const form = forms.find(f =>
    f.querySelector('[name="vendorId"]') &&
    f.querySelector('[name="serviceId"]') &&
    f.querySelector('[name="title"]') &&
    f.querySelector('[name="basePrice"]')
  );
  if (!form) return;

  const fv = new FormValidator('form[method="post"]'); // chọn form đầu tiên có method=post
  if (!fv || !fv.form) return;

  // 1) Vendor/Service bắt buộc
  fv.addRule('vendorId', 'required', 'Vui lòng chọn vendor');
  fv.addRule('serviceId', 'required', 'Vui lòng chọn dịch vụ');

  // 2) Tiêu đề
  fv.addRule('title', 'required', 'Vui lòng nhập tiêu đề');
  fv.addRule('title', 'minLength:3', 'Tiêu đề phải có ít nhất 3 ký tự');
  fv.addRule('title', 'maxLength:150', 'Tiêu đề tối đa 150 ký tự');

  // 3) Đơn vị tính (optional)
  if (form.querySelector('[name="unit"]')) {
    fv.addRule('unit', 'maxLength:50', 'Đơn vị tính tối đa 50 ký tự');
  }

  // 4) Giá cơ bản
  // - required
  // - minValue:0 (cho phép 0 nếu chính sách cho phép)
  // - pattern số tiền: số nguyên hoặc có 1 dấu chấm và tối đa 2 chữ số thập phân
  const moneyPattern = '^\\d+(\\.\\d{1,2})?$';
  fv.addRule('basePrice', 'required', 'Vui lòng nhập giá cơ bản');
  fv.addRule('basePrice', 'minValue:0', 'Giá cơ bản phải >= 0');
  fv.addRule('basePrice', `pattern:${moneyPattern}`, 'Giá cơ bản chỉ chấp nhận số, tối đa 2 chữ số thập phân');

  // 5) Thời lượng (optional): số nguyên dương, >=1
  if (form.querySelector('[name="durationMinutes"]')) {
    fv.addRule('durationMinutes', 'integer', 'Thời lượng phải là số nguyên dương');
    fv.addRule('durationMinutes', 'minValue:1', 'Thời lượng tối thiểu là 1 phút');
  }

  // 6) Báo trước tối thiểu (optional): số nguyên không âm, <=168
  if (form.querySelector('[name="minNoticeHours"]')) {
    fv.addRule('minNoticeHours', 'integer', 'Báo trước phải là số nguyên không âm');
    fv.addRule('minNoticeHours', 'minValue:0', 'Báo trước tối thiểu là 0 giờ');
    fv.addRule('minNoticeHours', 'maxValue:168', 'Không vượt quá 168 giờ');
  }

  // 7) Số job tối đa/ngày (optional): số nguyên không âm, <=100
  if (form.querySelector('[name="maxDailyJobs"]')) {
    fv.addRule('maxDailyJobs', 'integer', 'Số job/ngày phải là số nguyên không âm');
    fv.addRule('maxDailyJobs', 'minValue:0', 'Số job/ngày tối thiểu là 0');
    fv.addRule('maxDailyJobs', 'maxValue:100', 'Không vượt quá 100');
  }

  // 8) Mô tả (optional): tối đa 1000 ký tự
  if (form.querySelector('[name="description"]')) {
    fv.addRule('description', 'maxLength:1000', 'Mô tả tối đa 1000 ký tự');
  }

  // 9) Trạng thái (optional): ACTIVE|HIDDEN|PAUSED
  if (form.querySelector('[name="status"]')) {
    const statusPattern = '^(ACTIVE|HIDDEN|PAUSED)$';
    fv.addRule('status', `pattern:${statusPattern}`, 'Trạng thái không hợp lệ');
  }

  // 10) Ảnh bìa: coverUrl (optional) + coverFile (optional)
  // - Nếu có URL => phải là ảnh http(s) .jpg/.jpeg/.png/.webp
  // - Nếu có file => kiểm tra loại ảnh và dung lượng <= 5MB
  const coverUrlEl = form.querySelector('[name="coverUrl"]');
  const coverFileEl = form.querySelector('[name="coverFile"]');
  const imageUrlPattern = '^https?:\\/\\/[^\\s]+\\.(jpg|jpeg|png|webp)(\\?.*)?$';

  if (coverUrlEl) {
    fv.addRule('coverUrl', `pattern:${imageUrlPattern}`, 'URL ảnh phải là http(s) và kết thúc bằng .jpg/.jpeg/.png/.webp');
  }

  const MAX_FILE_MB = 5;
  const IMAGE_MIME = ['image/jpeg', 'image/png', 'image/webp'];
  const IMAGE_EXT = ['.jpg', '.jpeg', '.png', '.webp'];

  const hasAllowedExt = (name) => {
    const lower = (name || '').toLowerCase();
    return IMAGE_EXT.some(ext => lower.endsWith(ext));
  };

  const validateCoverFile = () => {
    if (!coverFileEl) return true;
    fv.clearError(coverFileEl);

    const f = coverFileEl.files && coverFileEl.files[0];
    if (!f) return true; // optional: bỏ qua nếu không chọn

    const okType = IMAGE_MIME.includes(f.type) || hasAllowedExt(f.name);
    if (!okType) {
      fv.showError(coverFileEl, 'Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP');
      return false;
    }
    const sizeMb = f.size / (1024 * 1024);
    if (sizeMb > MAX_FILE_MB) {
      fv.showError(coverFileEl, `Ảnh "${f.name}" vượt quá ${MAX_FILE_MB}MB`);
      return false;
    }
    return true;
  };

  if (coverFileEl) {
    coverFileEl.addEventListener('change', () => fv.clearError(coverFileEl));
  }

  // Hook submit bổ sung kiểm tra file
  form.addEventListener('submit', (e) => {
    const okFile = validateCoverFile();
    if (!okFile) e.preventDefault();
  });
});
