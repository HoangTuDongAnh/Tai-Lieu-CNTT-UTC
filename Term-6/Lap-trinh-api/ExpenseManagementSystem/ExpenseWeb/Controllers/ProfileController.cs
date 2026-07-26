using ExpenseWeb.Models.Dtos.Auth;
using ExpenseWeb.Models.Dtos.Support;
using ExpenseWeb.Models.ViewModels.Profile;
using ExpenseWeb.Services.Api;
using Microsoft.AspNetCore.Mvc;

namespace ExpenseWeb.Controllers
{
    public class ProfileController : Controller
    {
        private readonly AuthApiService _authApiService;
        private readonly SupportApiService _supportApiService;

        private const int MaxSupportFiles = 10;
        private const long MaxSupportFileSize = 20 * 1024 * 1024; // 20 MB

        private static readonly HashSet<string> AllowedSupportExtensions = new(StringComparer.OrdinalIgnoreCase)
        {
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
            ".mp4", ".mov", ".avi", ".mkv", ".webm",
            ".pdf", ".doc", ".docx"
        };

        public ProfileController(AuthApiService authApiService, SupportApiService supportApiService)
        {
            _authApiService = authApiService;
            _supportApiService = supportApiService;
        }

        private string? GetAccessToken()
        {
            return HttpContext.Session.GetString("AccessToken");
        }

        private bool HasAccessToken() => !string.IsNullOrWhiteSpace(GetAccessToken());

        private void SetProfileViewData(string? fullName = null)
        {
            ViewBag.UserFullName = fullName ?? HttpContext.Session.GetString("UserFullName") ?? "Người dùng";
        }

        private static (string lastName, string firstName) SplitFullName(string? fullName)
        {
            if (string.IsNullOrWhiteSpace(fullName))
                return (string.Empty, string.Empty);

            var parts = fullName.Trim().Split(' ', StringSplitOptions.RemoveEmptyEntries);
            if (parts.Length == 1)
                return (string.Empty, parts[0]);

            var firstName = parts[^1];
            var lastName = string.Join(" ", parts.Take(parts.Length - 1));
            return (lastName, firstName);
        }

        [HttpGet]
        public async Task<IActionResult> Index()
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token))
                return RedirectToAction("Login", "Auth");

            var result = await _authApiService.GetMeAsync(token);
            if (!result.Success || result.Data == null)
            {
                HttpContext.Session.Clear();
                return RedirectToAction("Login", "Auth");
            }

            var (lastName, firstName) = SplitFullName(result.Data.full_name);
            HttpContext.Session.SetString("UserFullName", result.Data.full_name ?? string.Empty);
            HttpContext.Session.SetString("UserAvatar", result.Data.avatar ?? string.Empty);

            var model = new ProfilePageViewModel
            {
                LastName = lastName,
                FirstName = firstName,
                Email = result.Data.email ?? string.Empty,
                PhoneNumber = result.Data.phone_number,
                Avatar = result.Data.avatar
            };

            SetProfileViewData(result.Data.full_name);

            var defaultAvatarsFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "sneat", "img", "avatars", "default");
            var defaultAvatars = new List<string>();
            if (Directory.Exists(defaultAvatarsFolder))
            {
                var files = Directory.GetFiles(defaultAvatarsFolder);
                foreach (var file in files)
                {
                    defaultAvatars.Add("/sneat/img/avatars/default/" + Path.GetFileName(file));
                }
            }
            ViewBag.DefaultAvatars = defaultAvatars;

            return View(model);
        }

        [HttpPost]
        public async Task<IActionResult> UpdateAjax([FromForm] ProfilePageViewModel model, IFormFile? AvatarFile)
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token))
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });

            if (model == null)
                return BadRequest(new { success = false, message = "Dữ liệu không hợp lệ." });

            var fullName = string.Join(" ", new[] { model.LastName?.Trim(), model.FirstName?.Trim() }
                .Where(x => !string.IsNullOrWhiteSpace(x)));

            if (string.IsNullOrWhiteSpace(fullName))
                return BadRequest(new { success = false, message = "Họ tên không được để trống." });

            string finalAvatarUrl = string.IsNullOrWhiteSpace(model.Avatar) ? string.Empty : model.Avatar.Trim();

            if (AvatarFile != null && AvatarFile.Length > 0)
            {
                try
                {
                    var uploadsFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "sneat", "img", "avatars", "upload");
                    if (!Directory.Exists(uploadsFolder))
                    {
                        Directory.CreateDirectory(uploadsFolder);
                    }

                    var userEmail = string.IsNullOrWhiteSpace(model.Email) ? "unknown" : model.Email.Trim();
                    var fileName = $"{userEmail}.png";
                    var filePath = Path.Combine(uploadsFolder, fileName);

                    if (System.IO.File.Exists(filePath))
                    {
                        System.IO.File.Delete(filePath);
                    }

                    using (var stream = new FileStream(filePath, FileMode.Create))
                    {
                        await AvatarFile.CopyToAsync(stream);
                    }

                    finalAvatarUrl = "/sneat/img/avatars/upload/" + fileName;
                }
                catch (Exception ex)
                {
                    return BadRequest(new { success = false, message = "Lỗi khi lưu ảnh: " + ex.Message });
                }
            }

            var request = new UpdateProfileRequestDto
            {
                full_name = fullName,
                email = model.Email?.Trim() ?? string.Empty,
                phone_number = string.IsNullOrWhiteSpace(model.PhoneNumber) ? null : model.PhoneNumber.Trim(),
                avatar = string.IsNullOrWhiteSpace(finalAvatarUrl) ? null : finalAvatarUrl
            };

            var result = await _authApiService.UpdateProfileAsync(token, request);
            if (!result.Success || result.Data == null)
                return BadRequest(new { success = false, message = result.ErrorMessage ?? "Không thể cập nhật hồ sơ." });

            HttpContext.Session.SetString("UserFullName", result.Data.full_name ?? string.Empty);
            HttpContext.Session.SetString("UserEmail", result.Data.email ?? string.Empty);
            HttpContext.Session.SetString("UserAvatar", finalAvatarUrl ?? string.Empty);

            return Json(new { success = true, message = "Cập nhật hồ sơ thành công." });
        }

        [HttpPost]
        public async Task<IActionResult> DeleteAccountAjax()
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token))
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });

            var result = await _authApiService.DeleteMeAsync(token);
            if (!result.Success)
                return BadRequest(new { success = false, message = result.ErrorMessage ?? "Không thể xóa tài khoản." });

            HttpContext.Session.Clear();
            return Json(new { success = true, redirectUrl = Url.Action("Login", "Auth") });
        }

        [HttpGet]
        public async Task<IActionResult> Support()
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token))
                return RedirectToAction("Login", "Auth");

            var model = await BuildSupportPageViewModelAsync(token, new SupportCreateFormViewModel());
            SetProfileViewData();
            return View(model);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Support(SupportCreateFormViewModel form)
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token))
                return RedirectToAction("Login", "Auth");

            ValidateSupportFiles(form.Files);

            if (!ModelState.IsValid)
            {
                var invalidModel = await BuildSupportPageViewModelAsync(token, form);
                invalidModel.ErrorMessage = "Vui lòng kiểm tra lại dữ liệu đã nhập.";
                SetProfileViewData();
                return View(invalidModel);
            }

            List<SupportAttachmentUploadDto> attachments;
            try
            {
                attachments = await SaveSupportFilesAsync(form.Files);
            }
            catch (Exception ex)
            {
                var errorModel = await BuildSupportPageViewModelAsync(token, form);
                errorModel.ErrorMessage = "Lỗi khi lưu tệp đính kèm: " + ex.Message;
                SetProfileViewData();
                return View(errorModel);
            }

            var createRequest = new SupportRequestCreateRequestDto
            {
                subject = form.Subject.Trim(),
                message = form.Message.Trim(),
                support_type = form.SupportType,
                priority = form.Priority,
                attachments = attachments
            };

            var createResult = await _supportApiService.CreateRequestAsync(token, createRequest);
            if (!createResult.Success)
            {
                var errorModel = await BuildSupportPageViewModelAsync(token, form);
                errorModel.ErrorMessage = createResult.ErrorMessage;
                SetProfileViewData();
                return View(errorModel);
            }

            TempData["SupportSuccess"] = "Gửi yêu cầu hỗ trợ thành công.";
            return RedirectToAction(nameof(Support));
        }

        private void ValidateSupportFiles(List<IFormFile>? files)
        {
            if (files == null || files.Count == 0)
                return;

            if (files.Count > MaxSupportFiles)
            {
                ModelState.AddModelError("Form.Files", $"Bạn chỉ được tải tối đa {MaxSupportFiles} tệp.");
                return;
            }

            foreach (var file in files)
            {
                if (file == null || file.Length <= 0)
                {
                    ModelState.AddModelError("Form.Files", "Có tệp không hợp lệ hoặc rỗng.");
                    continue;
                }

                if (file.Length > MaxSupportFileSize)
                {
                    ModelState.AddModelError("Form.Files", $"Tệp '{file.FileName}' vượt quá dung lượng tối đa 20 MB.");
                }

                var extension = Path.GetExtension(file.FileName);
                if (string.IsNullOrWhiteSpace(extension) || !AllowedSupportExtensions.Contains(extension))
                {
                    ModelState.AddModelError("Form.Files", $"Tệp '{file.FileName}' không đúng định dạng được hỗ trợ (ảnh, video, PDF, DOC, DOCX).");
                }
            }
        }

        private async Task<List<SupportAttachmentUploadDto>> SaveSupportFilesAsync(List<IFormFile>? files)
        {
            var result = new List<SupportAttachmentUploadDto>();

            if (files == null || files.Count == 0)
                return result;

            var now = DateTime.Now;
            var relativeFolder = $"/uploads/support/{now:yyyy}/{now:MM}";
            var physicalFolder = Path.Combine(
                Directory.GetCurrentDirectory(),
                "wwwroot",
                "uploads",
                "support",
                now.ToString("yyyy"),
                now.ToString("MM")
            );

            if (!Directory.Exists(physicalFolder))
            {
                Directory.CreateDirectory(physicalFolder);
            }

            foreach (var file in files)
            {
                if (file == null || file.Length <= 0)
                    continue;

                var extension = Path.GetExtension(file.FileName);
                var safeFileName = $"{Guid.NewGuid():N}{extension}";
                var physicalPath = Path.Combine(physicalFolder, safeFileName);

                using (var stream = new FileStream(physicalPath, FileMode.Create))
                {
                    await file.CopyToAsync(stream);
                }

                var fileUrl = $"{relativeFolder}/{safeFileName}".Replace("\\", "/");

                result.Add(new SupportAttachmentUploadDto
                {
                    file_name = Path.GetFileName(file.FileName),
                    file_url = fileUrl,
                    file_type = GetContentType(extension),
                    file_size = file.Length
                });
            }

            return result;
        }

        private async Task<SupportPageViewModel> BuildSupportPageViewModelAsync(string token, SupportCreateFormViewModel form)
        {
            var model = new SupportPageViewModel
            {
                Form = form,
                Lang = "vi",
                UserFullName = HttpContext.Session.GetString("UserFullName") ?? "Người dùng",
                SuccessMessage = TempData["SupportSuccess"]?.ToString()
            };

            try
            {
                var historyResult = await _supportApiService.GetMyRequestsAsync(token);
                if (!historyResult.Success)
                {
                    model.ErrorMessage = historyResult.ErrorMessage;
                    return model;
                }

                var detailTasks = historyResult.Data.Select(item =>
                    _supportApiService.GetMyRequestDetailAsync(token, item.support_request_id)
                ).ToList();

                var detailResults = await Task.WhenAll(detailTasks);

                for (int i = 0; i < historyResult.Data.Count; i++)
                {
                    var item = historyResult.Data[i];
                    var viewModel = MapHistoryItem(item);
                    var detailResult = detailResults[i];

                    if (detailResult.Success && detailResult.Data != null)
                    {
                        viewModel.Message = detailResult.Data.message ?? string.Empty;
                        viewModel.AdminReply = detailResult.Data.admin_reply;
                        viewModel.RepliedAt = detailResult.Data.replied_at;
                        viewModel.Attachments = detailResult.Data.attachments.Select(a => new SupportAttachmentViewModel
                        {
                            AttachmentId = a.attachment_id,
                            FileName = a.file_name,
                            FileUrl = a.file_url,
                            FileType = a.file_type,
                            FileSize = a.file_size,
                            DisplaySize = FormatFileSize(a.file_size),
                            IsImage = IsImage(a.file_type),
                            IsVideo = IsVideo(a.file_type),
                            IsPdf = IsPdf(a.file_type, a.file_name),
                            IsDocument = IsDocument(a.file_type, a.file_name)
                        }).ToList();
                    }

                    model.HistoryItems.Add(viewModel);
                }
            }
            catch (Exception ex)
            {
                // Trang vẫn load, không crash
                model.ErrorMessage = "Không thể tải lịch sử hỗ trợ. Vui lòng thử lại sau.";
            }

            return model;
        }

        private static SupportHistoryItemViewModel MapHistoryItem(SupportRequestListItemDto dto)
        {
            return new SupportHistoryItemViewModel
            {
                SupportRequestId = dto.support_request_id,
                Subject = dto.subject,
                Message = dto.message ?? string.Empty,
                SupportType = dto.support_type,
                SupportTypeLabel = ToSupportTypeLabel(dto.support_type),
                Priority = dto.priority,
                PriorityLabel = ToPriorityLabel(dto.priority),
                Status = dto.status,
                StatusLabel = ToStatusLabel(dto.status),
                StatusBadgeClass = ToStatusBadgeClass(dto.status),
                CreatedAt = dto.created_at,
                RepliedAt = dto.replied_at,
                AdminReply = dto.admin_reply,
                Attachments = new List<SupportAttachmentViewModel>()
            };
        }

        private static bool IsImage(string? fileType)
            => !string.IsNullOrWhiteSpace(fileType) &&
               fileType.StartsWith("image/", StringComparison.OrdinalIgnoreCase);

        private static bool IsVideo(string? fileType)
            => !string.IsNullOrWhiteSpace(fileType) &&
               fileType.StartsWith("video/", StringComparison.OrdinalIgnoreCase);

        private static bool IsPdf(string? fileType, string? fileName = null)
            => (!string.IsNullOrWhiteSpace(fileType) && fileType.Equals("application/pdf", StringComparison.OrdinalIgnoreCase))
               || string.Equals(Path.GetExtension(fileName ?? string.Empty), ".pdf", StringComparison.OrdinalIgnoreCase);

        private static bool IsDocument(string? fileType, string? fileName = null)
        {
            if (!string.IsNullOrWhiteSpace(fileType))
            {
                if (fileType.Equals("application/msword", StringComparison.OrdinalIgnoreCase)
                    || fileType.Equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", StringComparison.OrdinalIgnoreCase))
                {
                    return true;
                }
            }

            var extension = Path.GetExtension(fileName ?? string.Empty);
            return string.Equals(extension, ".doc", StringComparison.OrdinalIgnoreCase)
                || string.Equals(extension, ".docx", StringComparison.OrdinalIgnoreCase);
        }

        private static string FormatFileSize(long? bytes)
        {
            if (!bytes.HasValue || bytes.Value <= 0) return string.Empty;

            double len = bytes.Value;
            string[] units = { "B", "KB", "MB", "GB" };
            int order = 0;

            while (len >= 1024 && order < units.Length - 1)
            {
                order++;
                len /= 1024;
            }

            return $"{len:0.#} {units[order]}";
        }

        private static string GetContentType(string extension) => extension.ToLowerInvariant() switch
        {
            ".jpg" => "image/jpeg",
            ".jpeg" => "image/jpeg",
            ".png" => "image/png",
            ".gif" => "image/gif",
            ".bmp" => "image/bmp",
            ".webp" => "image/webp",
            ".mp4" => "video/mp4",
            ".mov" => "video/quicktime",
            ".avi" => "video/x-msvideo",
            ".mkv" => "video/x-matroska",
            ".webm" => "video/webm",
            ".pdf" => "application/pdf",
            ".doc" => "application/msword",
            ".docx" => "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            _ => "application/octet-stream"
        };

        private static string ToSupportTypeLabel(string value) => value switch
        {
            "bug" => "Báo lỗi hệ thống",
            "transaction" => "Hỗ trợ giao dịch",
            "account" => "Hỗ trợ tài khoản",
            "feature" => "Góp ý tính năng",
            _ => "Khác"
        };

        private static string ToPriorityLabel(string value) => value switch
        {
            "low" => "Thấp",
            "medium" => "Trung bình",
            "high" => "Cao",
            "urgent" => "Khẩn cấp",
            _ => value
        };

        private static string ToStatusLabel(string value) => value switch
        {
            "pending" => "Chờ tiếp nhận",
            "viewed" => "Đang xử lý",
            "replied" => "Đã phản hồi",
            "closed" => "Đã đóng",
            _ => value
        };

        private static string ToStatusBadgeClass(string value) => value switch
        {
            "pending" => "bg-label-primary",
            "viewed" => "bg-label-warning",
            "replied" => "bg-label-success",
            "closed" => "bg-label-secondary",
            _ => "bg-label-secondary"
        };
    }
}