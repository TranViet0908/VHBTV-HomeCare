// Validator cho form Đăng ký nhà cung cấp (customer/vendor/apply)
// Chú ý: Toàn bộ comment viết bằng tiếng Việt để đội dev dễ theo dõi

document.addEventListener('DOMContentLoaded', () => {
  // Tìm form theo action cố định của trang apply
  const formSelector = 'form[action="/customer/vendor/apply/submit"]';
  const form = document.querySelector(formSelector);
  if (!form) {
    // Không tìm thấy form thì bỏ qua
    return;
  }

  // Gài honeypot chống bot: field ẩn, nếu có giá trị sẽ chặn submit
  // (Kỹ thuật này giúp giảm spam tự động mà không ảnh hưởng UX)
  let honeypot = form.querySelector('input[name="website"]');
  if (!honeypot) {
    honeypot = document.createElement('input');
    honeypot.type = 'text';
    honeypot.name = 'website';
    honeypot.autocomplete = 'off';
    honeypot.tabIndex = -1;
    honeypot.setAttribute('aria-hidden', 'true');
    honeypot.style.position = 'absolute';
    honeypot.style.left = '-9999px';
    honeypot.style.opacity = '0';
    honeypot.style.height = '0';
    honeypot.style.width = '0';
    form.prepend(honeypot);
  }

  // Ghi nhận thời điểm người dùng mở trang để chặn submit quá nhanh (anti-bot)
  const startTs = Date.now();
  let submitted = false; // Chặn double-submit

  // Khởi tạo validator dùng core.js
  const validator = new FormValidator(formSelector);

  // QUY TẮC CHUNG AN TOÀN ĐẦU VÀO
  // - Không cho phép ký tự góc < > trong các trường text để giảm nguy cơ XSS
  // - Giới hạn độ dài hợp lý để tránh tấn công payload lớn
  // - Các trường không bắt buộc: chỉ validate khi có giá trị (đã được core xử lý)

  // 1) displayName - bắt buộc, độ dài và chặn ký tự nguy hiểm
  validator.addRule('displayName', 'required', 'Vui lòng nhập tên hiển thị.');
  validator.addRule('displayName', 'minLength:2', 'Tên hiển thị phải có ít nhất 2 ký tự.');
  validator.addRule('displayName', 'maxLength:100', 'Tên hiển thị tối đa 100 ký tự.');
  validator.addRule('displayName', 'pattern:^(?!.*[<>]).*$', 'Tên hiển thị không được chứa ký tự < hoặc >.');

  // 2) fullName - bắt buộc, tên tiếng Việt, độ dài và chặn ký tự nguy hiểm
  validator.addRule('fullName', 'required', 'Vui lòng nhập họ và tên.');
  validator.addRule('fullName', 'vietnameseFullName', 'Họ và tên chỉ gồm chữ cái tiếng Việt và khoảng trắng.');
  validator.addRule('fullName', 'minLength:2', 'Họ và tên phải có ít nhất 2 ký tự.');
  validator.addRule('fullName', 'maxLength:100', 'Họ và tên tối đa 100 ký tự.');
  validator.addRule('fullName', 'pattern:^(?!.*[<>]).*$', 'Họ và tên không được chứa ký tự < hoặc >.');

  // 3) email - không bắt buộc, nhưng nếu có thì phải hợp lệ
  validator.addRule('email', 'email', 'Email không hợp lệ.');
  validator.addRule('email', 'maxLength:150', 'Email tối đa 150 ký tự.');
  validator.addRule('email', 'pattern:^(?!.*[<>]).*$', 'Email không được chứa ký tự < hoặc >.');

  // 4) phone - không bắt buộc, nếu có thì theo định dạng VN (0 + 9 số)
  validator.addRule('phone', 'phone', 'Số điện thoại không hợp lệ (10 chữ số, bắt đầu bằng 0).');

  // 5) address - không bắt buộc, giới hạn độ dài và chặn ký tự nguy hiểm
  validator.addRule('address', 'maxLength:255', 'Địa chỉ tối đa 255 ký tự.');
  validator.addRule('address', 'pattern:^(?!.*[<>]).*$', 'Địa chỉ không được chứa ký tự < hoặc >.');

  // 6) region - không bắt buộc, giới hạn độ dài và chặn ký tự nguy hiểm
  validator.addRule('region', 'maxLength:100', 'Khu vực tối đa 100 ký tự.');
  validator.addRule('region', 'pattern:^(?!.*[<>]).*$', 'Khu vực không được chứa ký tự < hoặc >.');

  // 7) experienceYears - không bắt buộc, nhưng nếu có phải là số nguyên không âm, giới hạn hợp lý
  validator.addRule('experienceYears', 'integer', 'Số năm kinh nghiệm phải là số nguyên không âm.');
  validator.addRule('experienceYears', 'minValue:0', 'Số năm kinh nghiệm không được âm.');
  validator.addRule('experienceYears', 'maxValue:80', 'Số năm kinh nghiệm tối đa 80.');

  // 8) note - không bắt buộc, giới hạn độ dài và chặn ký tự nguy hiểm
  validator.addRule('note', 'maxLength:1000', 'Ghi chú tối đa 1000 ký tự.');
  validator.addRule('note', 'pattern:^(?!.*[<>]).*$', 'Ghi chú không được chứa ký tự < hoặc >.');

  // Hàm làm sạch giá trị đầu vào: bỏ ký tự nguy hiểm, khoảng trắng thừa, từ khóa nguy cơ
  const sanitizeValue = (val) => {
    if (!val) return '';
    let v = String(val);
    // Loại bỏ dấu góc để tránh chèn thẻ HTML/script
    v = v.replace(/[<>]/g, '');
    // Loại bỏ các protocol nguy hiểm
    v = v.replace(/(javascript:|data:|vbscript:)/gi, '');
    // Loại bỏ ký tự điều khiển vô hình
    v = v.replace(/[\u0000-\u001F\u007F]/g, '');
    // Chuẩn hóa khoảng trắng
    v = v.replace(/\s{2,}/g, ' ').trim();
    return v;
  };

  // Áp dụng sanitize theo thời gian thực cho các trường văn bản tự do
  const fieldsToSanitize = ['displayName', 'fullName', 'address', 'region', 'note'];
  fieldsToSanitize.forEach((name) => {
    const el = form.querySelector(`[name="${name}"]`);
    if (el) {
      el.addEventListener('input', () => {
        const cleaned = sanitizeValue(el.value);
        if (el.value !== cleaned) el.value = cleaned;
      });
      el.addEventListener('blur', () => {
        el.value = sanitizeValue(el.value);
      });
    }
  });

  // Trim gọn cho email/phone
  ['email', 'phone'].forEach((name) => {
    const el = form.querySelector(`[name="${name}"]`);
    if (el) {
      el.addEventListener('blur', () => {
        el.value = el.value.trim();
      });
    }
  });

  // Hiển thị lỗi tổng quát ở đầu form (thân thiện người dùng)
  const showFormError = (msg) => {
    let bar = form.querySelector('.form-global-error');
    if (!bar) {
      bar = document.createElement('div');
      bar.className = 'form-global-error bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl mb-4';
      form.prepend(bar);
    }
    bar.textContent = msg;
    // Tự ẩn sau 5 giây để không chiếm chỗ quá lâu
    setTimeout(() => {
      if (bar && bar.parentNode) {
        bar.remove();
      }
    }, 5000);
  };

  // Lắng nghe submit ở capture-phase để chặn spinner nếu form chưa hợp lệ
  form.addEventListener(
    'submit',
    (e) => {
      // Chặn submit lặp lại
      if (submitted) {
        e.preventDefault();
        e.stopPropagation();
        return;
      }

      // Anti-bot: honeypot không được có dữ liệu
      if (honeypot && honeypot.value && honeypot.value.trim() !== '') {
        e.preventDefault();
        e.stopPropagation();
        showFormError('Hệ thống phát hiện hành vi không hợp lệ. Vui lòng thử lại.');
        return;
      }

      // Anti-bot: Thời gian điền form tối thiểu 2 giây
      const delta = Date.now() - startTs;
      if (delta < 2000) {
        e.preventDefault();
        e.stopPropagation();
        showFormError('Bạn thao tác quá nhanh. Vui lòng kiểm tra lại thông tin.');
        return;
      }

      // Thực hiện validate các trường
      if (!validator.validate()) {
        e.preventDefault();
        e.stopPropagation();
        // Focus vào trường đầu tiên bị lỗi (nếu có)
        const firstError = form.querySelector('.border-red-500');
        if (firstError) firstError.focus();
        return;
      }

      // Đánh dấu đã submit hợp lệ, tránh submit 2 lần
      submitted = true;
    },
    true // Capture phase để chặn handler spinner phía dưới nếu invalid
  );
});

