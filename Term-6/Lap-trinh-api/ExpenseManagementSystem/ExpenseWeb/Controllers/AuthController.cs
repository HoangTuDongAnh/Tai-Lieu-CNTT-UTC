using System.Text.Json;
using ExpenseWeb.Models.Dtos.Auth;
using ExpenseWeb.Models.ViewModels.Auth;
using ExpenseWeb.Services.Api;
using Microsoft.AspNetCore.Mvc;

namespace ExpenseWeb.Controllers
{
    public class AuthController : Controller
    {
        private readonly AuthApiService _authApiService;

        public AuthController(AuthApiService authApiService)
        {
            _authApiService = authApiService;
        }

        private string GetLang()
        {
            // 1. Ưu tiên lấy từ Query String (?lang=en)
            string lang = Request.Query["lang"];

            // 2. Nếu không có, thử lấy từ Session
            if (string.IsNullOrEmpty(lang))
            {
                lang = HttpContext.Session.GetString("CurrentLanguage");
            }

            // 3. CHỈ đọc Form nếu là request POST và có dữ liệu Form hợp lệ
            // Tránh lỗi "This request does not have a Content-Type header"
            if (string.IsNullOrEmpty(lang) && Request.Method == "POST" && Request.HasFormContentType)
            {
                lang = Request.Form["lang"];
            }

            return (lang?.ToLower() == "en") ? "en" : "vi";
        }

        private List<string> GetModelErrors()
        {
            return ModelState.Values
                .SelectMany(v => v.Errors)
                .Select(e => e.ErrorMessage)
                .Where(x => !string.IsNullOrWhiteSpace(x))
                .Distinct()
                .ToList();
        }

        private void SetToastErrors(string key, IEnumerable<string> errors)
        {
            var cleanErrors = errors
                .Where(x => !string.IsNullOrWhiteSpace(x))
                .Distinct()
                .ToList();

            ViewData[key] = cleanErrors.Count > 0 ? JsonSerializer.Serialize(cleanErrors) : "[]";
        }

        private string GetAuthText(string key, string lang)
        {
            lang = (lang ?? "vi").ToLower() == "en" ? "en" : "vi";

            return key switch
            {
                "LoginFailed" => lang == "en" ? "Invalid email or password." : "Email hoặc mật khẩu không đúng.",
                "RegisterFailed" => lang == "en" ? "Registration failed." : "Đăng ký thất bại.",
                "OtpInvalid" => lang == "en" ? "Invalid OTP." : "Mã OTP không hợp lệ.",
                "ForgotFailed" => lang == "en" ? "Failed to send password reset request." : "Không thể gửi yêu cầu đặt lại mật khẩu.",
                "ResetFailed" => lang == "en" ? "Password reset failed." : "Đặt lại mật khẩu thất bại.",
                _ => string.Empty
            };
        }

        private IActionResult RedirectAfterLogin(string? lang = null)
        {
            lang = (lang ?? GetLang()).ToLower() == "en" ? "en" : "vi";
            var role = HttpContext.Session.GetString("UserRole")?.Trim().ToLower();

            if (role == "admin")
            {
                return RedirectToAction("Index", "Admin", new { lang });
            }

            return RedirectToAction("Index", "Dashboard", new { lang });
        }

        [HttpGet]
        public IActionResult Login()
        {
            if (!string.IsNullOrEmpty(HttpContext.Session.GetString("AccessToken")))
            {
                return RedirectAfterLogin(GetLang());
            }

            ViewBag.RegisterModel = new RegisterViewModel();
            ViewBag.OpenRegister = false;
            ViewData["LoginErrorsJson"] = "[]";
            ViewData["RegisterErrorsJson"] = "[]";

            return View(new LoginViewModel());
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Login(LoginViewModel model, string? lang = "vi")
        {
            lang = (lang ?? "vi").ToLower() == "en" ? "en" : "vi";
            ViewBag.RegisterModel = new RegisterViewModel();
            ViewBag.OpenRegister = false;
            ViewData["RegisterErrorsJson"] = "[]";

            if (!ModelState.IsValid)
            {
                SetToastErrors("LoginErrorsJson", GetModelErrors());
                return View(model);
            }

            var result = await _authApiService.LoginAsync(new LoginRequestDto
            {
                email = model.Email,
                password = model.Password
            });

            if (!result.Success || result.Data == null)
            {
                SetToastErrors("LoginErrorsJson", new[] { GetAuthText("LoginFailed", lang) });
                return View(model);
            }

            HttpContext.Session.Clear();
            HttpContext.Session.SetString("AccessToken", result.Data.access_token ?? "");
            HttpContext.Session.SetString("UserEmail", result.Data.user?.email ?? "");
            HttpContext.Session.SetString("UserFullName", result.Data.user?.full_name ?? "");
            HttpContext.Session.SetString("UserRole", result.Data.user?.role ?? "");
            HttpContext.Session.SetString("UserAvatar", result.Data.user?.avatar ?? "/sneat/img/avatars/default/teams_1.png");

            return RedirectAfterLogin(lang);
        }

        [HttpGet]
        public IActionResult Register()
        {
            return RedirectToAction("Login", new { lang = GetLang(), openRegister = true });
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Register(RegisterViewModel model, string? lang = "vi")
        {
            lang = (lang ?? "vi").ToLower() == "en" ? "en" : "vi";
            ViewBag.RegisterModel = model;
            ViewBag.OpenRegister = true;
            ViewData["LoginErrorsJson"] = "[]";

            if (!model.AgreeTerms)
            {
                ModelState.AddModelError(nameof(model.AgreeTerms), lang == "en" ? "You must agree to the terms." : "Bạn phải đồng ý với điều khoản sử dụng.");
            }

            if (!ModelState.IsValid)
            {
                SetToastErrors("RegisterErrorsJson", GetModelErrors());
                return View("Login", new LoginViewModel());
            }

            var result = await _authApiService.RegisterAsync(new RegisterRequestDto
            {
                full_name = model.FullName,
                email = model.Email,
                password = model.Password,
                phone_number = null,
                avatar = null,
                agree_terms = model.AgreeTerms
            });

            if (!result.Success)
            {
                SetToastErrors("RegisterErrorsJson", new[] { result.ErrorMessage ?? GetAuthText("RegisterFailed", lang) });
                return View("Login", new LoginViewModel());
            }

            HttpContext.Session.SetString("TempEmail", model.Email ?? "");
            HttpContext.Session.SetString("TempPassword", model.Password ?? "");

            return RedirectToAction("VerifyOtp", new { email = model.Email, lang });
        }

        [HttpGet]
        public IActionResult VerifyOtp(string email)
        {
            if (string.IsNullOrWhiteSpace(email)) return RedirectToAction("Login", new { lang = GetLang() });
            return View(new VerifyOtpViewModel { Email = email });
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> VerifyOtp(VerifyOtpViewModel model, string? lang = "vi")
        {
            lang = (lang ?? "vi").ToLower() == "en" ? "en" : "vi";
            if (!ModelState.IsValid) return View(model);

            var result = await _authApiService.VerifyOtpAsync(model.Email, model.Otp);
            if (!result.Success)
            {
                ModelState.AddModelError(string.Empty, result.ErrorMessage ?? GetAuthText("OtpInvalid", lang));
                return View(model);
            }

            var email = HttpContext.Session.GetString("TempEmail");
            var password = HttpContext.Session.GetString("TempPassword");
            if (!string.IsNullOrEmpty(email) && !string.IsNullOrEmpty(password))
            {
                var loginResult = await _authApiService.LoginAsync(new LoginRequestDto { email = email, password = password });
                if (loginResult.Success && loginResult.Data != null)
                {
                    HttpContext.Session.Clear();
                    HttpContext.Session.SetString("AccessToken", loginResult.Data.access_token ?? "");
                    HttpContext.Session.SetString("UserEmail", loginResult.Data.user?.email ?? "");
                    HttpContext.Session.SetString("UserFullName", loginResult.Data.user?.full_name ?? "");
                    HttpContext.Session.SetString("UserRole", loginResult.Data.user?.role ?? "");
                    HttpContext.Session.SetString("UserAvatar", loginResult.Data.user?.avatar ?? "/sneat/img/avatars/default/teams_1.png");

                    HttpContext.Session.Remove("TempEmail");
                    HttpContext.Session.Remove("TempPassword");
                    return RedirectAfterLogin(lang);
                }
            }

            TempData["SuccessMessage"] = lang == "en" ? "Verification successful. Please sign in." : "Xác thực thành công. Vui lòng đăng nhập.";
            return RedirectToAction("Login", new { lang });
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> ResendOtp(string email, string? lang = "vi")
        {
            lang = (lang ?? "vi").ToLower() == "en" ? "en" : "vi";
            if (string.IsNullOrWhiteSpace(email)) return RedirectToAction("Login", new { lang });

            var result = await _authApiService.ResendOtpAsync(email);
            TempData[result.Success ? "SuccessMessage" : "ErrorMessage"] = result.Success
                ? (lang == "en" ? "OTP resent successfully." : "Gửi lại OTP thành công.")
                : (result.ErrorMessage ?? (lang == "en" ? "Failed to resend OTP." : "Gửi lại OTP thất bại."));

            return RedirectToAction("VerifyOtp", new { email, lang });
        }

        [HttpGet]
        public IActionResult ForgotPassword(string? lang = null)
        {
            ViewBag.Lang = (lang ?? GetLang()) == "en" ? "en" : "vi";
            return View(new ForgotPasswordViewModel());
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> ForgotPassword(ForgotPasswordViewModel model, string? lang = "vi")
        {
            lang = (lang ?? "vi").ToLower() == "en" ? "en" : "vi";
            if (!ModelState.IsValid) return View(model);

            var result = await _authApiService.ForgotPasswordAsync(model.Email);
            if (!result.Success)
            {
                TempData["ErrorMessage"] = result.ErrorMessage ?? GetAuthText("ForgotFailed", lang);
                return View(model);
            }

            TempData["SuccessMessage"] = lang == "en" ? "Request sent. Check your email." : "Yêu cầu đã được gửi. Kiểm tra email của bạn.";
            return RedirectToAction("ForgotPasswordConfirmation", new { lang });
        }

        [HttpGet]
        public IActionResult ForgotPasswordConfirmation() => View();

        [HttpGet]
        public IActionResult ResetPassword(string token, string? lang = "vi")
        {
            if (string.IsNullOrWhiteSpace(token)) return RedirectToAction("Login", new { lang });
            return View(new ResetPasswordViewModel { Token = token });
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> ResetPassword(ResetPasswordViewModel model, string? lang = "vi")
        {
            lang = (lang ?? "vi").ToLower() == "en" ? "en" : "vi";
            if (!ModelState.IsValid) return View(model);

            var result = await _authApiService.ResetPasswordAsync(model.Token, model.NewPassword);
            if (!result.Success)
            {
                ModelState.AddModelError(string.Empty, result.ErrorMessage ?? GetAuthText("ResetFailed", lang));
                return View(model);
            }

            TempData["SuccessMessage"] = lang == "en" ? "Success! Please sign in." : "Thành công! Vui lòng đăng nhập.";
            return RedirectToAction("Login", new { lang });
        }

        [HttpPost]
        public IActionResult SetTokenToSession([FromBody] TokenSyncRequest request)
        {
            if (request == null) return BadRequest();
            HttpContext.Session.SetString("AccessToken", request.AccessToken);
            HttpContext.Session.SetString("UserRole", request.Role ?? "admin");
            HttpContext.Session.SetString("UserEmail", request.Email ?? "");
            HttpContext.Session.SetString("UserFullName", request.FullName ?? "");
            HttpContext.Session.SetString("UserAvatar", request.Avatar ?? "/sneat/img/avatars/default/teams_1.png");
            return Ok(new { success = true });
        }

        [HttpGet, HttpPost]
        public IActionResult Logout(string? lang = null)
        {
            // 1. Xóa sạch Session C#
            HttpContext.Session.Clear();

            // 2. Lấy ngôn ngữ an toàn để quay về trang Login
            var currentLang = (lang ?? GetLang());

            // 3. Chuyển hướng
            return RedirectToAction("Login", new { lang = currentLang });
        }

        [HttpGet]
        public IActionResult Profile() => RedirectToAction("Index", "Dashboard", new { lang = GetLang() });

        public class TokenSyncRequest
        {
            public string AccessToken { get; set; }
            public string Role { get; set; }
            public string Email { get; set; }
            public string FullName { get; set; }
            public string Avatar { get; set; }
        }
    }
}