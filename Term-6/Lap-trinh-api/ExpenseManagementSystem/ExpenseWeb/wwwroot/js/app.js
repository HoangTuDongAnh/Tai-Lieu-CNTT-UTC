window.App = window.App || {};
window.App.version = 'ui-standardization-starter';

document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('[data-app-auto-dismiss]').forEach(function (el) {
    var delay = parseInt(el.getAttribute('data-app-auto-dismiss') || '4000', 10);
    window.setTimeout(function () {
      el.remove();
    }, delay);
  });
});
