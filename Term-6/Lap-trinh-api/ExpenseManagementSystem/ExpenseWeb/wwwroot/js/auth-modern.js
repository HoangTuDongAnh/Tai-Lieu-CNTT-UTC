document.addEventListener("DOMContentLoaded", function () {
    const authLang = (document.documentElement.lang || "vi").toLowerCase() === "en" ? "en" : "vi";
    const authSlider = document.getElementById("authSlider");
    const toastStack = document.getElementById("authToastStack");

    const i18n = {
        vi: {
            titles: {
                error: "Lỗi",
                success: "Thành công",
                loginError: "Lỗi đăng nhập",
                registerError: "Lỗi đăng ký",
                verifyError: "Lỗi xác thực"
            },
            messages: {
                "The Email field is required.": "Email là bắt buộc.",
                "The Password field is required.": "Mật khẩu là bắt buộc.",
                "The FullName field is required.": "Họ và tên là bắt buộc.",
                "The NewPassword field is required.": "Mật khẩu mới là bắt buộc.",
                "The ConfirmPassword field is required.": "Xác nhận mật khẩu là bắt buộc.",
                "The Token field is required.": "Liên kết đặt lại mật khẩu không hợp lệ.",
                "The Email field is not a valid e-mail address.": "Định dạng email không hợp lệ.",
                "The Email field is not a valid email address.": "Định dạng email không hợp lệ.",
                "Email is required.": "Email là bắt buộc.",
                "Password is required.": "Mật khẩu là bắt buộc.",
                "Full name is required.": "Họ và tên là bắt buộc.",
                "Confirm password is required.": "Xác nhận mật khẩu là bắt buộc.",
                "Invalid email format.": "Định dạng email không hợp lệ.",
                "Password must be at least 6 characters.": "Mật khẩu phải có ít nhất 6 ký tự.",
                "The field NewPassword must be a string or array type with a minimum length of '6'.": "Mật khẩu mới phải có ít nhất 6 ký tự.",
                "The field Password must be a string or array type with a minimum length of '6'.": "Mật khẩu phải có ít nhất 6 ký tự.",
                "Passwords do not match": "Mật khẩu xác nhận không khớp.",
                "Confirm password does not match.": "Mật khẩu xác nhận không khớp.",
                "You must agree to the terms.": "Bạn phải đồng ý với điều khoản sử dụng.",
                "OTP is required.": "Mã OTP là bắt buộc.",
                "OTP must be 4 to 6 digits.": "Mã OTP phải gồm từ 4 đến 6 ký tự.",
                "Invalid email or password": "Email hoặc mật khẩu không đúng.",
                "Invalid email or password.": "Email hoặc mật khẩu không đúng.",
                "Account is not active": "Email hoặc mật khẩu không đúng.",
                "Account is not active.": "Email hoặc mật khẩu không đúng.",
                "Email already exists": "Email đã tồn tại.",
                "Invalid or expired token": "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.",
                "Password must be at least 6 characters": "Mật khẩu phải có ít nhất 6 ký tự.",
                "Password reset successfully": "Đặt lại mật khẩu thành công.",
                "OTP verified successfully": "Xác thực OTP thành công."
            },
            client: {
                loginEmailRequired: "Email là bắt buộc.",
                loginEmailInvalid: "Định dạng email không hợp lệ.",
                loginPasswordRequired: "Mật khẩu là bắt buộc.",
                fullNameRequired: "Họ và tên là bắt buộc.",
                emailRequired: "Email là bắt buộc.",
                emailInvalid: "Định dạng email không hợp lệ.",
                passwordRequired: "Mật khẩu là bắt buộc.",
                passwordMin: "Mật khẩu phải có ít nhất 6 ký tự.",
                confirmRequired: "Xác nhận mật khẩu là bắt buộc.",
                confirmMismatch: "Mật khẩu xác nhận không khớp.",
                agreeTerms: "Bạn phải đồng ý với điều khoản sử dụng.",
                otpRequired: "Mã OTP là bắt buộc.",
                otpInvalid: "Mã OTP phải gồm từ 4 đến 6 ký tự.",
                tokenRequired: "Liên kết đặt lại mật khẩu không hợp lệ.",
                newPasswordRequired: "Mật khẩu mới là bắt buộc.",
                newPasswordMin: "Mật khẩu mới phải có ít nhất 6 ký tự."
            },
            loadingFallback: "Đang xử lý..."
        },
        en: {
            titles: {
                error: "Error",
                success: "Success",
                loginError: "Login error",
                registerError: "Register error",
                verifyError: "Verification error"
            },
            messages: {},
            client: {
                loginEmailRequired: "Email is required.",
                loginEmailInvalid: "Invalid email format.",
                loginPasswordRequired: "Password is required.",
                fullNameRequired: "Full name is required.",
                emailRequired: "Email is required.",
                emailInvalid: "Invalid email format.",
                passwordRequired: "Password is required.",
                passwordMin: "Password must be at least 6 characters.",
                confirmRequired: "Confirm password is required.",
                confirmMismatch: "Confirm password does not match.",
                agreeTerms: "You must agree to the terms.",
                otpRequired: "OTP is required.",
                otpInvalid: "OTP must be 4 to 6 digits.",
                tokenRequired: "Invalid reset password token.",
                newPasswordRequired: "New password is required.",
                newPasswordMin: "New password must be at least 6 characters."
            },
            loadingFallback: "Processing..."
        }
    };

    function dict() {
        return i18n[authLang] || i18n.vi;
    }

    function normalizeMessage(message) {
        if (!message) return "";
        const raw = String(message).trim();
        return dict().messages[raw] || raw;
    }

    function uniqueMessages(messages) {
        const seen = new Set();
        return (messages || [])
            .map(normalizeMessage)
            .filter(Boolean)
            .filter(function (message) {
                if (seen.has(message)) return false;
                seen.add(message);
                return true;
            });
    }

    function showToast(title, messages, type) {
        const safeMessages = uniqueMessages(Array.isArray(messages) ? messages : [messages]);
        if (!toastStack || !safeMessages.length) return;

        const toast = document.createElement("div");
        toast.className = "auth-toast " + (type === "success" ? "auth-toast-success" : "auth-toast-error");
        const bodyList = safeMessages.map(function (m) { return "<li>" + m + "</li>"; }).join("");

        toast.innerHTML = `
            <div class="auth-toast-header">
                <span>${title}</span>
                <button type="button" class="auth-toast-close" aria-label="Close">×</button>
            </div>
            <div class="auth-toast-body">
                <ul>${bodyList}</ul>
            </div>
        `;

        toast.querySelector(".auth-toast-close")?.addEventListener("click", function () {
            toast.remove();
        });

        toastStack.appendChild(toast);
        setTimeout(function () { toast.remove(); }, 5000);
    }

    window.AuthToast = {
        error: function (title, messages) {
            showToast(title || dict().titles.error, messages, "error");
        },
        success: function (title, messages) {
            showToast(title || dict().titles.success, messages, "success");
        }
    };

    window.AuthPage = {
        lang: authLang,
        t: function (key) {
            return dict().titles[key] || key;
        },
        showMessages: function (title, messages, type) {
            showToast(title, messages, type);
        },
        normalizeMessage: normalizeMessage
    };

    document.querySelectorAll("[data-password-toggle]").forEach(function (button) {
        button.addEventListener("click", function () {
            var targetId = button.getAttribute("data-password-toggle");
            var input = document.getElementById(targetId);
            if (!input) return;

            var icon = button.querySelector("i");

            if (input.type === "password") {
                input.type = "text";
                if (icon) {
                    icon.classList.remove("bx-hide");
                    icon.classList.add("bx-show");
                }
            } else {
                input.type = "password";
                if (icon) {
                    icon.classList.remove("bx-show");
                    icon.classList.add("bx-hide");
                }
            }
        });
    });

    document.querySelectorAll("[data-auth-toggle='signup']").forEach(function (btn) {
        btn.addEventListener("click", function () {
            if (authSlider) authSlider.classList.add("right-panel-active");
        });
    });

    document.querySelectorAll("[data-auth-toggle='signin']").forEach(function (btn) {
        btn.addEventListener("click", function () {
            if (authSlider) authSlider.classList.remove("right-panel-active");
        });
    });

    function setLoadingState(form) {
        const button = form.querySelector("[data-submit-text]");
        if (!button) return;
        const loadingText = button.getAttribute("data-loading-text") || dict().loadingFallback;
        button.disabled = true;
        button.dataset.originalText = button.innerHTML;
        button.innerHTML = loadingText;
    }

    function validEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function collectErrors(form) {
        const kind = form.getAttribute("data-auth-form");
        const c = dict().client;
        const errors = [];
        const email = (form.querySelector('input[name="Email"], input[name="email"], #RegisterEmail')?.value || "").trim();
        const password = (form.querySelector('input[name="Password"], input[name="password"]')?.value || "").trim();
        const fullName = (form.querySelector('input[name="FullName"]')?.value || "").trim();
        const confirmPassword = (form.querySelector('input[name="ConfirmPassword"]')?.value || "").trim();
        const newPassword = (form.querySelector('input[name="NewPassword"]')?.value || "").trim();
        const otp = (form.querySelector('input[name="Otp"]')?.value || "").trim();
        const token = (form.querySelector('input[name="Token"]')?.value || "").trim();
        const agreeTerms = form.querySelector('input[name="AgreeTerms"]');

        if (kind === "login") {
            if (!email) errors.push(c.loginEmailRequired);
            else if (!validEmail(email)) errors.push(c.loginEmailInvalid);
            if (!password) errors.push(c.loginPasswordRequired);
        }

        if (kind === "register") {
            if (!fullName) errors.push(c.fullNameRequired);
            if (!email) errors.push(c.emailRequired);
            else if (!validEmail(email)) errors.push(c.emailInvalid);
            if (!password) errors.push(c.passwordRequired);
            else if (password.length < 6) errors.push(c.passwordMin);
            if (!confirmPassword) errors.push(c.confirmRequired);
            else if (password && confirmPassword !== password) errors.push(c.confirmMismatch);
            if (agreeTerms && !agreeTerms.checked) errors.push(c.agreeTerms);
        }

        if (kind === "forgot-password") {
            if (!email) errors.push(c.emailRequired);
            else if (!validEmail(email)) errors.push(c.emailInvalid);
        }

        if (kind === "verify-otp") {
            if (!email) errors.push(c.emailRequired);
            else if (!validEmail(email)) errors.push(c.emailInvalid);
            if (!otp) errors.push(c.otpRequired);
            else if (otp.length < 4 || otp.length > 6) errors.push(c.otpInvalid);
        }

        if (kind === "reset-password") {
            if (!token) errors.push(c.tokenRequired);
            if (!newPassword) errors.push(c.newPasswordRequired);
            else if (newPassword.length < 6) errors.push(c.newPasswordMin);
            if (!confirmPassword) errors.push(c.confirmRequired);
            else if (newPassword && confirmPassword !== newPassword) errors.push(c.confirmMismatch);
        }

        return uniqueMessages(errors);
    }

    document.querySelectorAll("form[data-auth-form]").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            const errors = collectErrors(form);
            if (errors.length) {
                event.preventDefault();
                const kind = form.getAttribute("data-auth-form");
                const titleKey = kind === "login" ? "loginError" : kind === "register" ? "registerError" : kind === "verify-otp" ? "verifyError" : "error";
                window.AuthToast.error(dict().titles[titleKey] || dict().titles.error, errors);
                return;
            }

            setLoadingState(form);
        });
    });
});
