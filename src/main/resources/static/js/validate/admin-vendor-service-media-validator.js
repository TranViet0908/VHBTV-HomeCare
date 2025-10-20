document.addEventListener('DOMContentLoaded', () => {

  // Chấp nhận: .mp4/.webm hoặc YouTube/Drive có query/hash tùy ý
  const VIDEO_URL_PATTERN =
    '^https?:\\/\\/(?:' +
      '(?:[^\\s]+?\\.(?:mp4|webm)(?:[?#][^\\s]*)?)' + // file trực tiếp
      '|' +
      '(?:(?:[^\\/]+\\.)?youtube\\.com\\/(?:' +       // youtube.com
          'watch\\?v=[A-Za-z0-9_-]{6,}(?:[&?#][^\\s]*)?' +
          '|shorts\\/[A-Za-z0-9_-]{6,}(?:[?&#][^\\s]*)?' +
          '|embed\\/[A-Za-z0-9_-]{6,}(?:[?&#][^\\s]*)?' +
        '))' +
      '|' +
      '(?:youtu\\.be\\/[A-Za-z0-9_-]{6,}(?:[?&#][^\\s]*)?)' + // youtu.be
      '|' +
      '(?:drive\\.google\\.com\\/(?:' +              // Google Drive
          'file\\/d\\/[A-Za-z0-9_-]+\\/(?:view|preview)' +
          '|(?:open|uc)\\?id=[A-Za-z0-9_-]+' +
        ')(?:[?&#][^\\s]*)?)' +
    ')$';

  // ========== FORM 1: UPLOAD ẢNH (#imgForm) ==========
  const imgForm = document.getElementById('imgForm');
  if (imgForm) {
    const validator = new FormValidator(imgForm);
    validator.addRule('vsId', 'required', 'Vui lòng chọn dịch vụ');

    imgForm.addEventListener('submit', (e) => {
      let hasError = false;

      const vendor1 = document.getElementById('vendor1');
      if (!vendor1.value) { new FormValidator(imgForm).showError(vendor1, 'Vui lòng chọn vendor'); hasError = true; }

      const filesInput = document.getElementById('filesInput1');
      const fileError = validateImageFiles(filesInput);
      if (fileError) { new FormValidator(imgForm).showError(filesInput, fileError); hasError = true; }

      if (hasError) e.preventDefault();
    });

    document.getElementById('vendor1').addEventListener('change', function(){ new FormValidator(imgForm).clearError(this); });
    document.getElementById('filesInput1').addEventListener('change', function(){ new FormValidator(imgForm).clearError(this); });
  }

  // ========== FORM 2: THÊM VIDEO (#vidForm) ==========
  const vidForm = document.getElementById('vidForm');
  if (vidForm) {
    const validator = new FormValidator(vidForm);

    validator.addRule('vsId', 'required', 'Vui lòng chọn dịch vụ');
    validator.addRule('url', 'required', 'Vui lòng nhập URL video');
    validator.addRule('url', 'pattern:' + VIDEO_URL_PATTERN,
      'URL phải là http(s) và thuộc YouTube, Google Drive hoặc kết thúc bằng .mp4/.webm');

    validator.addRule('altText', 'maxLength:255', 'Mô tả (alt) tối đa 255 ký tự');
    validator.addRule('sortOrder', 'integer', 'Thứ tự phải là số nguyên không âm');
    validator.addRule('sortOrder', 'minValue:0', 'Thứ tự phải >= 0');

    vidForm.addEventListener('submit', (e) => {
      const vendor2 = document.getElementById('vendor2');
      if (!vendor2.value) { validator.showError(vendor2, 'Vui lòng chọn vendor'); e.preventDefault(); }
    });

    document.getElementById('vendor2').addEventListener('change', function(){ validator.clearError(this); });
    const urlInput2 = document.getElementById('urlInput');
    urlInput2?.addEventListener('input', function(){ validator.clearError(this); });
  }

  // ========== FORM 3: EDIT MEDIA (edit.html) ==========
  const editForm = document.querySelector('form[action*="/admin/vendor-service-media/"][action*="/update"]');
  if (editForm) {
    const validator = new FormValidator(editForm);
    const urlInput = editForm.querySelector('input[name="url"]');

    if (urlInput && !urlInput.disabled) {
      validator.addRule('url', 'required', 'Vui lòng nhập URL video');
      validator.addRule('url', 'pattern:' + VIDEO_URL_PATTERN,
        'URL phải là http(s) và thuộc YouTube, Google Drive hoặc kết thúc bằng .mp4/.webm');
      urlInput.addEventListener('input', function(){ validator.clearError(this); });
    }

    validator.addRule('altText', 'maxLength:255', 'Mô tả (alt) tối đa 255 ký tự');
    validator.addRule('sortOrder', 'integer', 'Thứ tự phải là số nguyên không âm');
    validator.addRule('sortOrder', 'minValue:0', 'Thứ tự phải >= 0');
  }

  // ========== HELPER ==========
  function validateImageFiles(inputElement) {
    const files = inputElement.files;
    if (!files || files.length === 0) return 'Vui lòng chọn ít nhất 1 ảnh';
    if (files.length > 10) return 'Bạn chỉ được chọn tối đa 10 ảnh';

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    const allowedExtensions = ['.jpg', '.jpeg', '.png', '.webp'];
    const maxSizeBytes = 5 * 1024 * 1024;

    for (let i = 0; i < files.length; i++) {
      const f = files[i];
      const okType = allowedTypes.includes(f.type) || allowedExtensions.some(ext => f.name.toLowerCase().endsWith(ext));
      if (!okType) return 'Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP';
      if (f.size > maxSizeBytes) return `Ảnh "${f.name}" vượt quá 5MB`;
    }
    return null;
  }
});
