

using ExpenseWeb.Filters;
using ExpenseWeb.Models.Dtos.Admin;
using ExpenseWeb.Models.Dtos.Auth;
using ExpenseWeb.Models.Dtos.Support;
using ExpenseWeb.Models.ViewModels.Admin;
using ExpenseWeb.Models.ViewModels.Profile;
using ExpenseWeb.Services.Api;
using Microsoft.AspNetCore.Mvc;
using System.IO;
using System.Net;

namespace ExpenseWeb.Controllers
{
    [RequireRole("admin")]
    public class AdminController : Controller
    {
        private readonly SupportApiService _supportApiService;
        private readonly AuthApiService _authApiService;
        private readonly AdminApiService _adminApiService;

        public AdminController(SupportApiService supportApiService, AuthApiService authApiService, AdminApiService adminApiService)
        {
            _supportApiService = supportApiService;
            _authApiService = authApiService;
            _adminApiService = adminApiService;
        }

        private string? GetAccessToken() => HttpContext.Session.GetString("AccessToken");

        public async Task<IActionResult> Index()
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token))
                return RedirectToAction("Login", "Auth");

            var result = await _adminApiService.GetDashboardAsync(token);
            if (!result.Success || result.Data == null)
            {
                if (result.StatusCode == HttpStatusCode.Unauthorized)
                {
                    HttpContext.Session.Clear();
                    return RedirectToAction("Login", "Auth");
                }

                TempData["AdminDashboardError"] = result.ErrorMessage;
                return View(new AdminDashboardViewModel());
            }

            return View(MapDashboard(result.Data));
        }

        [HttpGet]
        public async Task<IActionResult> DashboardData()
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token))
                return Unauthorized(new { message = "Phiên đăng nhập đã hết hạn." });

            var result = await _adminApiService.GetDashboardAsync(token);
            if (!result.Success || result.Data == null)
            {
                if (result.StatusCode == HttpStatusCode.Unauthorized)
                {
                    HttpContext.Session.Clear();
                    return Unauthorized(new { message = "Phiên đăng nhập đã hết hạn." });
                }

                return StatusCode((int)(((int)result.StatusCode == 0) ? HttpStatusCode.BadGateway : result.StatusCode), new
                {
                    message = string.IsNullOrWhiteSpace(result.ErrorMessage) ? "Không thể tải dữ liệu dashboard." : result.ErrorMessage
                });
            }

            return Json(MapDashboard(result.Data));
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

        private static AdminDashboardViewModel MapDashboard(AdminDashboardDto dto)
        {
            return new AdminDashboardViewModel
            {
                TotalUsers = dto.total_users,
                ActiveUsers = dto.active_users,
                InactiveUsers = dto.inactive_users,
                PendingRequests = dto.pending_requests,
                NewUsersToday = dto.new_users_today,
                NewUsersWeekTotal = dto.new_users_week_total,
                HighPriorityOpenRequests = dto.high_priority_open_requests,
                LastUpdatedAt = dto.last_updated_at,
                NewUsersWeek = dto.new_users_week ?? new List<int>(),
                NewUsersWeekLabels = dto.new_users_week_labels ?? new List<string>(),
                RecentUsers = dto.recent_users?.Select(MapDashboardUser).ToList() ?? new List<AdminUserItemViewModel>(),
                RecentPendingRequests = dto.recent_pending_requests?.Select(MapSupportQueueItem).ToList() ?? new List<AdminSupportQueueItemViewModel>()
            };
        }

        private static AdminUserItemViewModel MapDashboardUser(AdminDashboardUserDto dto)
        {
            return new AdminUserItemViewModel
            {
                UserId = dto.user_id,
                FullName = dto.full_name,
                Email = dto.email,
                PhoneNumber = dto.phone_number,
                Role = dto.role,
                Status = dto.status,
                Avatar = dto.avatar,
                CreatedAt = dto.created_at
            };
        }

        private static AdminSupportQueueItemViewModel MapSupportQueueItem(AdminDashboardSupportRequestDto dto)
        {
            return new AdminSupportQueueItemViewModel
            {
                SupportRequestId = dto.support_request_id,
                Subject = dto.subject,
                SupportType = dto.support_type,
                Priority = dto.priority,
                Status = dto.status,
                CreatedAt = dto.created_at,
                UserId = dto.user_id,
                UserFullName = dto.user_full_name,
                UserEmail = dto.user_email,
                UserAvatar = dto.user_avatar
            };
        }

        [HttpGet]
        public async Task<IActionResult> Profile()
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

                    var userEmail = string.IsNullOrWhiteSpace(model.Email) ? "admin_unknown" : model.Email.Trim();
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



        [HttpGet]
        public async Task<IActionResult> GetSupportDetail(string id)
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token) || string.IsNullOrWhiteSpace(id))
                return BadRequest();

            var detailResult = await _supportApiService.GetAdminRequestDetailAsync(token, id);
            if (!detailResult.Success || detailResult.Data == null)
            {
                return NotFound();
            }

            var model = MapAdminDetail(detailResult.Data);

            return PartialView("_SupportRequestDetail", model);
        }

        [HttpGet]
        public async Task<IActionResult> ErrorReports(string? status, string? supportType, string? priority, string? keyword, string? selectedId)
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token))
                return RedirectToAction("Login", "Auth");

            var query = new SupportRequestAdminQueryDto
            {
                status = status,
                support_type = supportType,
                priority = priority,
                keyword = keyword,
                page = 1,
                page_size = 30
            };

            var listResult = await _supportApiService.GetAdminRequestsAsync(token, query);
            var model = new AdminErrorReportsPageViewModel
            {
                Lang = "vi",
                CurrentStatus = status,
                CurrentSupportType = supportType,
                CurrentPriority = priority,
                Keyword = keyword
            };

            if (!listResult.Success)
            {
                TempData["AdminSupportError"] = listResult.ErrorMessage;
                return View(model);
            }

            model.Items = listResult.Data.Select(MapAdminListItem).ToList();

            if (!string.IsNullOrWhiteSpace(selectedId))
            {
                var detailResult = await _supportApiService.GetAdminRequestDetailAsync(token, selectedId);
                if (detailResult.Success && detailResult.Data != null)
                {
                    model.SelectedItem = MapAdminDetail(detailResult.Data);
                }
                else
                {
                    TempData["AdminSupportError"] = detailResult.ErrorMessage;
                }
            }
            else if (model.Items.Count > 0)
            {
                var firstId = model.Items[0].SupportRequestId;
                var detailResult = await _supportApiService.GetAdminRequestDetailAsync(token, firstId);
                if (detailResult.Success && detailResult.Data != null)
                {
                    model.SelectedItem = MapAdminDetail(detailResult.Data);
                }
            }

            return View(model);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> ReplySupport(string supportRequestId, string status, string? adminReply)
        {
            var token = GetAccessToken();
            if (string.IsNullOrWhiteSpace(token))
                return RedirectToAction("Login", "Auth");

            if (string.IsNullOrWhiteSpace(supportRequestId))
            {
                TempData["AdminSupportError"] = "Thiếu mã yêu cầu hỗ trợ.";
                return RedirectToAction(nameof(ErrorReports));
            }

            var request = new SupportRequestAdminReplyRequestDto
            {
                status = string.IsNullOrWhiteSpace(status) ? "replied" : status,
                admin_reply = string.IsNullOrWhiteSpace(adminReply) ? null : adminReply.Trim()
            };

            var result = await _supportApiService.ReplyAsync(token, supportRequestId, request);
            if (!result.Success)
            {
                TempData["AdminSupportError"] = result.ErrorMessage;
            }
            else
            {
                TempData["AdminSupportSuccess"] = request.status switch
                {
                    "viewed" => "Đã cập nhật trạng thái đang xử lý.",
                    "closed" => "Đã đóng yêu cầu hỗ trợ.",
                    _ => "Cập nhật phản hồi hỗ trợ thành công."
                };
            }

            return RedirectToAction(nameof(ErrorReports), new { selectedId = supportRequestId });
        }

        private static string DecodeText(string? value) => string.IsNullOrWhiteSpace(value) ? string.Empty : WebUtility.HtmlDecode(value);

        private static bool IsPdf(string? fileType, string? fileName)
        {
            return (!string.IsNullOrWhiteSpace(fileType) && fileType.Equals("application/pdf", StringComparison.OrdinalIgnoreCase))
                || (!string.IsNullOrWhiteSpace(fileName) && fileName.EndsWith(".pdf", StringComparison.OrdinalIgnoreCase));
        }

        private static bool IsDocument(string? fileType, string? fileName)
        {
            if (!string.IsNullOrWhiteSpace(fileType))
            {
                if (fileType.Equals("application/msword", StringComparison.OrdinalIgnoreCase)
                    || fileType.Equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", StringComparison.OrdinalIgnoreCase))
                {
                    return true;
                }
            }

            return (!string.IsNullOrWhiteSpace(fileName) && (fileName.EndsWith(".doc", StringComparison.OrdinalIgnoreCase) || fileName.EndsWith(".docx", StringComparison.OrdinalIgnoreCase)));
        }

        private static AdminSupportRequestListItemViewModel MapAdminListItem(SupportRequestListItemDto dto)
        {
            return new AdminSupportRequestListItemViewModel
            {
                SupportRequestId = dto.support_request_id,
                Subject = DecodeText(dto.subject),
                UserFullName = DecodeText(string.IsNullOrWhiteSpace(dto.user_full_name) ? dto.user_id : dto.user_full_name),
                UserEmail = DecodeText(dto.user_email),
                SupportType = dto.support_type,
                SupportTypeLabel = ToSupportTypeLabel(dto.support_type),
                SupportTypeBadgeClass = ToSupportTypeBadgeClass(dto.support_type),
                Priority = dto.priority,
                PriorityLabel = ToPriorityLabel(dto.priority),
                PriorityBadgeClass = ToPriorityBadgeClass(dto.priority),
                Status = dto.status,
                StatusLabel = ToStatusLabel(dto.status),
                StatusBadgeClass = ToStatusBadgeClass(dto.status),
                CreatedAt = dto.created_at,
                HasAttachments = dto.has_attachments
            };
        }

        private static AdminSupportRequestDetailViewModel MapAdminDetail(SupportRequestDetailDto dto)
        {
            return new AdminSupportRequestDetailViewModel
            {
                SupportRequestId = dto.support_request_id,
                UserId = dto.user_id,
                UserFullName = DecodeText(string.IsNullOrWhiteSpace(dto.user_full_name) ? dto.user_id : dto.user_full_name),
                UserEmail = DecodeText(dto.user_email),
                UserAvatar = dto.user_avatar,
                Subject = DecodeText(dto.subject),
                Message = DecodeText(dto.message),
                SupportType = dto.support_type,
                SupportTypeLabel = ToSupportTypeLabel(dto.support_type),
                Priority = dto.priority,
                PriorityLabel = ToPriorityLabel(dto.priority),
                Status = dto.status,
                StatusLabel = ToStatusLabel(dto.status),
                AdminReply = DecodeText(dto.admin_reply),
                CreatedAt = dto.created_at,
                ViewedAt = dto.viewed_at,
                RepliedAt = dto.replied_at,
                ClosedAt = dto.closed_at,
                Attachments = dto.attachments.Select(a => new SupportAttachmentViewModel
                {
                    AttachmentId = a.attachment_id,
                    FileName = DecodeText(a.file_name),
                    FileUrl = a.file_url,
                    FileType = a.file_type,
                    FileSize = a.file_size,
                    DisplaySize = FormatFileSize(a.file_size),
                    IsImage = IsImage(a.file_type),
                    IsVideo = IsVideo(a.file_type),
                    IsPdf = IsPdf(a.file_type, a.file_name),
                    IsDocument = IsDocument(a.file_type, a.file_name)
                }).ToList()
            };
        }

        private static bool IsImage(string? fileType) => !string.IsNullOrWhiteSpace(fileType) && fileType.StartsWith("image/", StringComparison.OrdinalIgnoreCase);
        private static bool IsVideo(string? fileType) => !string.IsNullOrWhiteSpace(fileType) && fileType.StartsWith("video/", StringComparison.OrdinalIgnoreCase);

        private static string FormatFileSize(long? bytes)
        {
            if (!bytes.HasValue || bytes.Value <= 0) return string.Empty;
            var size = bytes.Value;
            string[] units = { "B", "KB", "MB", "GB" };
            var order = 0;
            double len = size;
            while (len >= 1024 && order < units.Length - 1)
            {
                order++;
                len = len / 1024;
            }
            return $"{len:0.#} {units[order]}";
        }

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

        private static string ToSupportTypeBadgeClass(string value) => value switch
        {
            "bug" => "bg-label-danger",
            "transaction" => "bg-label-primary",
            "account" => "bg-label-info",
            "feature" => "bg-label-success",
            _ => "bg-label-secondary"
        };

        private static string ToPriorityBadgeClass(string value) => value switch
        {
            "low" => "bg-label-secondary",
            "medium" => "bg-label-info",
            "high" => "bg-label-warning",
            "urgent" => "bg-label-danger",
            _ => "bg-label-secondary"
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