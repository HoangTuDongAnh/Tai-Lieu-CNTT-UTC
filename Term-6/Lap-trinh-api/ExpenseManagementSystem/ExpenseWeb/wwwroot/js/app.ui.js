(function () {
  function getCurrentLang() {
    var htmlLang = (document.documentElement.getAttribute('lang') || '').toLowerCase();
    if (htmlLang === 'en' || htmlLang === 'vi') return htmlLang;

    try {
      var url = new URL(window.location.href);
      var qLang = (url.searchParams.get('lang') || '').toLowerCase();
      if (qLang === 'en' || qLang === 'vi') return qLang;
    } catch (e) {}

    return 'vi';
  }

  function getDictionary() {
    var lang = getCurrentLang();
    return lang === 'en'
      ? {
          success: 'Success',
          error: 'Error',
          warning: 'Warning',
          info: 'Information',
          defaultTitle: 'Notification',
          close: 'Close',
          processing: 'Processing...'
        }
      : {
          success: 'Thành công',
          error: 'Lỗi',
          warning: 'Lưu ý',
          info: 'Thông tin',
          defaultTitle: 'Thông báo',
          close: 'Đóng',
          processing: 'Đang xử lý...'
        };
  }

  function getStack() {
    var stack = document.getElementById('appToastStack');
    if (!stack) {
      stack = document.createElement('div');
      stack.id = 'appToastStack';
      stack.className = 'app-toast-stack';
      document.body.appendChild(stack);
    }
    return stack;
  }

  function normalizeMessages(input) {
    if (Array.isArray(input)) return input.filter(Boolean);
    if (input === null || input === undefined) return [];
    return [String(input)];
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function showToast(message, type, options) {
    var opts = options || {};
    var messages = normalizeMessages(message);
    var stack = getStack();
    var toast = document.createElement('div');
    var toastType = type || 'success';
    var dict = getDictionary();
    var title = opts.title || (dict[toastType] || dict.defaultTitle);

    toast.className = 'app-toast app-toast--' + toastType;
    toast.innerHTML = [
      '<div class="app-toast__inner">',
      '  <div class="app-toast__head">',
      '    <div class="app-toast__title">' + escapeHtml(title) + '</div>',
      '    <button type="button" class="app-toast__close" aria-label="' + escapeHtml(dict.close) + '">×</button>',
      '  </div>',
      '  <div class="app-toast__body">' + messages.map(function (x) { return '<div>' + escapeHtml(x) + '</div>'; }).join('') + '</div>',
      '</div>'
    ].join('');

    toast.querySelector('.app-toast__close').addEventListener('click', function () {
      toast.remove();
    });

    stack.appendChild(toast);

    window.setTimeout(function () {
      if (toast && toast.parentNode) toast.remove();
    }, opts.duration || 4500);

    return toast;
  }

  function resolveModal(id) {
    var el = typeof id === 'string' ? document.getElementById(id) : id;
    return el ? bootstrap.Modal.getOrCreateInstance(el) : null;
  }

  function resolveOffcanvas(id) {
    var el = typeof id === 'string' ? document.getElementById(id) : id;
    return el ? bootstrap.Offcanvas.getOrCreateInstance(el) : null;
  }

  window.AppToast = {
    success: function (message, options) { return showToast(message, 'success', options); },
    error: function (message, options) { return showToast(message, 'error', options); },
    warning: function (message, options) { return showToast(message, 'warning', options); },
    info: function (message, options) { return showToast(message, 'info', options); },
    show: showToast,
    getLang: getCurrentLang
  };

  window.AppUi = {
    openModal: function (id) { var modal = resolveModal(id); if (modal) modal.show(); },
    closeModal: function (id) { var modal = resolveModal(id); if (modal) modal.hide(); },
    openOffcanvas: function (id) { var panel = resolveOffcanvas(id); if (panel) panel.show(); },
    closeOffcanvas: function (id) { var panel = resolveOffcanvas(id); if (panel) panel.hide(); },
    setButtonLoading: function (button, isLoading, loadingText) {
      if (!button) return;
      if (isLoading) {
        button.dataset.originalHtml = button.innerHTML;
        button.disabled = true;
        button.innerHTML = loadingText || getDictionary().processing;
      } else {
        button.disabled = false;
        if (button.dataset.originalHtml) button.innerHTML = button.dataset.originalHtml;
      }
    }
  };
})();
