document.addEventListener('DOMContentLoaded', function () {
  function getSlider() {
    return document.getElementById('authSlider');
  }

  function getLang() {
    if (window.AppToast && typeof window.AppToast.getLang === 'function') {
      return window.AppToast.getLang();
    }
    var htmlLang = (document.documentElement.getAttribute('lang') || '').toLowerCase();
    return htmlLang === 'en' ? 'en' : 'vi';
  }

  function dict() {
    var lang = getLang();
    return lang === 'en'
      ? {
          loginTitle: 'Sign in error',
          registerTitle: 'Registration error',
          fieldRequired: 'This field is required.',
          emailRequired: 'Email is required.',
          passwordRequired: 'Password is required.',
          fullNameRequired: 'Full name is required.',
          confirmPasswordRequired: 'Confirm password is required.',
          invalidEmail: 'Invalid email format.',
          termsRequired: 'You must agree to the terms and conditions.',
          passwordMismatch: 'Confirm password does not match.',
          otpRequired: 'OTP is required.',
          otpInvalid: 'OTP must contain 6 digits.',
          accountNotFound: 'Account does not exist.',
          wrongPassword: 'Incorrect password.',
          inactiveAccount: 'Account is not active.',
          invalidLogin: 'Invalid email or password.'
        }
      : {
          loginTitle: 'Lỗi đăng nhập',
          registerTitle: 'Lỗi đăng ký',
          fieldRequired: 'Trường này là bắt buộc.',
          emailRequired: 'Email là bắt buộc.',
          passwordRequired: 'Mật khẩu là bắt buộc.',
          fullNameRequired: 'Họ và tên là bắt buộc.',
          confirmPasswordRequired: 'Xác nhận mật khẩu là bắt buộc.',
          invalidEmail: 'Định dạng email không hợp lệ.',
          termsRequired: 'Bạn phải đồng ý với điều khoản và điều kiện sử dụng.',
          passwordMismatch: 'Xác nhận mật khẩu không khớp.',
          otpRequired: 'Mã OTP là bắt buộc.',
          otpInvalid: 'Mã OTP phải gồm 6 chữ số.',
          accountNotFound: 'Tài khoản không tồn tại.',
          wrongPassword: 'Mật khẩu không đúng.',
          inactiveAccount: 'Tài khoản chưa được kích hoạt.',
          invalidLogin: 'Email hoặc mật khẩu không đúng.'
        };
  }

  function openPanel(panel) {
    var container = getSlider();
    if (!container) return;
    if (panel === 'signup') {
      container.classList.add('right-panel-active');
      return;
    }
    container.classList.remove('right-panel-active');
  }

  function localizeMessage(message) {
    if (!message) return '';
    var d = dict();
    var map = {
      'The Email field is required.': d.emailRequired,
      'The Password field is required.': d.passwordRequired,
      'The FullName field is required.': d.fullNameRequired,
      'The ConfirmPassword field is required.': d.confirmPasswordRequired,
      'The Otp field is required.': d.otpRequired,
      'The Email field is not a valid e-mail address.': d.invalidEmail,
      'The Email must be a valid email address.': d.invalidEmail,
      'Invalid email format.': d.invalidEmail,
      'Email is required.': d.emailRequired,
      'Password is required.': d.passwordRequired,
      'Full name is required.': d.fullNameRequired,
      'Confirm password is required.': d.confirmPasswordRequired,
      'You must agree to the terms.': d.termsRequired,
      'You must agree to the terms and conditions.': d.termsRequired,
      'Bạn phải đồng ý với điều khoản sử dụng.': d.termsRequired,
      'Confirm password does not match.': d.passwordMismatch,
      'Passwords do not match': d.passwordMismatch,
      'OTP is required.': d.otpRequired,
      'OTP must contain 6 digits.': d.otpInvalid,
      'Account does not exist': d.accountNotFound,
      'Account does not exist.': d.accountNotFound,
      'Incorrect password': d.wrongPassword,
      'Incorrect password.': d.wrongPassword,
      'Account is not active': d.inactiveAccount,
      'Account is not active.': d.inactiveAccount,
      'Invalid email or password': d.invalidLogin,
      'Invalid email or password.': d.invalidLogin
    };

    if (map[message]) return map[message];

    return String(message)
      .replace(/^The Email field is required\.?$/, d.emailRequired)
      .replace(/^The Password field is required\.?$/, d.passwordRequired)
      .replace(/^The FullName field is required\.?$/, d.fullNameRequired)
      .replace(/^The ConfirmPassword field is required\.?$/, d.confirmPasswordRequired)
      .replace(/^The Email field is not a valid e-mail address\.?$/, d.invalidEmail);
  }

  function showMessages(items) {
    if (!Array.isArray(items) || !window.AppToast) return;

    items.forEach(function (item) {
      if (!item || !Array.isArray(item.messages) || item.messages.length === 0) return;
      var type = item.type || 'info';
      var title = item.title || undefined;
      var messages = item.messages.map(localizeMessage).filter(Boolean);
      if (messages.length === 0) return;
      if (typeof window.AppToast[type] === 'function') {
        window.AppToast[type](messages, { title: title });
      } else {
        window.AppToast.show(messages, type, { title: title });
      }
    });
  }

  function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value || '').trim());
  }

  function validateLoginForm(form) {
    if (!form) return [];
    var d = dict();
    var errors = [];
    var emailInput = form.querySelector('input[name="Email"]');
    var passwordInput = form.querySelector('input[name="Password"]');
    var email = emailInput ? emailInput.value.trim() : '';
    var password = passwordInput ? passwordInput.value : '';

    if (!email) errors.push(d.emailRequired);
    else if (!isValidEmail(email)) errors.push(d.invalidEmail);

    if (!password) errors.push(d.passwordRequired);
    return errors;
  }

  function bindLoginValidation() {
    var form = document.querySelector('form[asp-action="Login"], form[action*="/Auth/Login"], form[action*="/Auth/Login?"]') || document.querySelector('form.auth-card-form');
    if (!form) return;

    var emailInput = form.querySelector('input[name="Email"]');
    var passwordInput = form.querySelector('input[name="Password"]');
    if (!emailInput || !passwordInput) return;

    form.addEventListener('submit', function (event) {
      var errors = validateLoginForm(form);
      if (errors.length > 0) {
        event.preventDefault();
        window.AppToast.error(errors, { title: dict().loginTitle });
      }
    });
  }

  document.querySelectorAll('[data-password-toggle]').forEach(function (button) {
    button.addEventListener('click', function () {
      var targetId = button.getAttribute('data-password-toggle');
      var input = document.getElementById(targetId);
      if (!input) return;

      var icon = button.querySelector('i');
      var isPassword = input.type === 'password';
      input.type = isPassword ? 'text' : 'password';
      if (icon) {
        icon.classList.toggle('bx-hide', !isPassword);
        icon.classList.toggle('bx-show', isPassword);
      }
    });
  });

  document.querySelectorAll("[data-auth-toggle='signup']").forEach(function (btn) {
    btn.addEventListener('click', function () { openPanel('signup'); });
  });

  document.querySelectorAll("[data-auth-toggle='signin']").forEach(function (btn) {
    btn.addEventListener('click', function () { openPanel('signin'); });
  });

  bindLoginValidation();

  window.AuthPage = {
    openPanel: openPanel,
    showMessages: showMessages,
    localizeMessage: localizeMessage
  };
});
