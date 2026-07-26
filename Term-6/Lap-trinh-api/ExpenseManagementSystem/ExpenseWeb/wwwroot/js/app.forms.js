(function () {
  document.addEventListener('submit', function (event) {
    var form = event.target;
    if (!(form instanceof HTMLFormElement)) return;
    if (!form.hasAttribute('data-loading-button')) return;

    var button = form.querySelector('[data-submit-text]');
    if (!button) return;
    if (typeof form.checkValidity === 'function' && !form.checkValidity()) return;

    window.AppUi && window.AppUi.setButtonLoading(
      button,
      true,
      button.getAttribute('data-loading-text') || 'Đang xử lý...'
    );
  });
})();
