using System.Globalization;
using System.Text.Json;
using ExpenseWeb.Models.Dtos.Category;
using ExpenseWeb.Models.Dtos.Transaction;
using ExpenseWeb.Models.Dtos.Wallet;
using ExpenseWeb.Models.ViewModels.Transaction;
using ExpenseWeb.Services.Api;
using Microsoft.AspNetCore.Mvc;
using ExpenseWeb.Filters;

namespace ExpenseWeb.Controllers
{
    [RequireRole("user")]
    public class TransactionController : Controller
    {
        private readonly TransactionApiService _transactionApiService;
        private readonly CategoryApiService _categoryApiService;
        private readonly WalletApiService _walletApiService;

        public TransactionController(
            TransactionApiService transactionApiService,
            CategoryApiService categoryApiService,
            WalletApiService walletApiService)
        {
            _transactionApiService = transactionApiService;
            _categoryApiService = categoryApiService;
            _walletApiService = walletApiService;
        }

        [HttpGet]
        public async Task<IActionResult> Index()
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrWhiteSpace(token))
            {
                return RedirectToAction("Login", "Auth");
            }

            ViewBag.UserFullName = HttpContext.Session.GetString("UserFullName");
            var viewModel = await BuildPageViewModelAsync(token);
            return View(viewModel);
        }

        [HttpPost]
        public async Task<IActionResult> CreateAjax([FromBody] TransactionCreateRequestDto request)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrWhiteSpace(token))
            {
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });
            }

            NormalizeCreateRequest(request);
            var validationError = ValidateCreateRequest(request);
            if (validationError != null)
            {
                return BadRequest(new { success = false, message = validationError });
            }

            try
            {
                var created = await _transactionApiService.CreateTransactionAsync(token, request);
                return Json(new
                {
                    success = true,
                    message = "Thêm giao dịch thành công.",
                    data = created
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ExtractApiMessage(ex.Message, "Không thể thêm giao dịch.") });
            }
        }

        [HttpPut]
        public async Task<IActionResult> UpdateAjax(string id, [FromBody] TransactionUpdateRequestDto request)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrWhiteSpace(token))
            {
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });
            }

            if (string.IsNullOrWhiteSpace(id))
            {
                return BadRequest(new { success = false, message = "Thiếu mã giao dịch." });
            }

            NormalizeUpdateRequest(request);
            var validationError = ValidateUpdateRequest(request);
            if (validationError != null)
            {
                return BadRequest(new { success = false, message = validationError });
            }

            try
            {
                var updated = await _transactionApiService.UpdateTransactionAsync(token, id, request);
                return Json(new
                {
                    success = true,
                    message = "Cập nhật giao dịch thành công.",
                    data = updated
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ExtractApiMessage(ex.Message, "Không thể cập nhật giao dịch.") });
            }
        }

        [HttpDelete]
        public async Task<IActionResult> DeleteAjax(string id)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrWhiteSpace(token))
            {
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });
            }

            if (string.IsNullOrWhiteSpace(id))
            {
                return BadRequest(new { success = false, message = "Thiếu mã giao dịch." });
            }

            try
            {
                await _transactionApiService.DeleteTransactionAsync(token, id);
                return Json(new { success = true, message = "Xóa giao dịch thành công." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ExtractApiMessage(ex.Message, "Không thể xóa giao dịch.") });
            }
        }

        [HttpPost]
        public async Task<IActionResult> TransferAjax([FromBody] TransferCreateRequestDto request)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrWhiteSpace(token))
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });

            if (!ModelState.IsValid)
            {
                var firstError = ModelState.Values.SelectMany(v => v.Errors).FirstOrDefault()?.ErrorMessage;
                return BadRequest(new { success = false, message = firstError ?? "Dữ liệu không hợp lệ." });
            }

            if (request.from_wallet_id == request.to_wallet_id)
                return BadRequest(new { success = false, message = "Ví nguồn và ví đích không được trùng nhau." });

            try
            {
                var result = await _transactionApiService.TransferAsync(token, request);
                return Json(new { success = true, message = "Chuyển tiền thành công.", data = result });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ExtractApiMessage(ex.Message, "Không thể chuyển tiền.") });
            }
        }

        private async Task<TransactionPageViewModel> BuildPageViewModelAsync(string token)
        {
            var viewModel = new TransactionPageViewModel();

            try
            {
                var transactionsTask = _transactionApiService.GetTransactionsAsync(token);

                var categoriesTask = _categoryApiService.GetCategoriesAsync(token, false);

                var walletsTask = _walletApiService.GetWalletsAsync(token);

                await Task.WhenAll(transactionsTask, categoriesTask, walletsTask);

                viewModel.Categories = categoriesTask.Result;
                viewModel.Wallets = walletsTask.Result;

                viewModel.Transactions = BuildTransactionItems(
                    transactionsTask.Result,
                    viewModel.Categories,
                    viewModel.Wallets);
            }
            catch (Exception ex)
            {
                viewModel.ErrorMessage = ExtractApiMessage(ex.Message, "Không tải được dữ liệu giao dịch từ API.");
            }

            return viewModel;
        }

        private static List<TransactionItemViewModel> BuildTransactionItems(
    List<TransactionResponseDto> transactions,
    List<CategoryResponseDto> categories,
    List<WalletResponseDto> wallets)
        {
            var culture = CultureInfo.GetCultureInfo("vi-VN");

            return transactions.Select(t =>
            {
                var category = categories.FirstOrDefault(x => x.category_id == t.category_id);
                var wallet = wallets.FirstOrDefault(x => x.wallet_id == t.wallet_id);

                // Nhận diện nếu là chuyển khoản
                bool isTransfer = (category?.category_name == "Chuyển tiền") ||
                                  (t.note != null && (t.note.StartsWith("Chuyển đến") || t.note.StartsWith("Nhận từ")));

                var currency = wallet?.currency ?? "VND";
                var signedAmount = t.transaction_type == "income" ? t.amount : -t.amount;

                return new TransactionItemViewModel
                {
                    TransactionId = t.transaction_id,
                    WalletId = t.wallet_id,
                    WalletName = wallet?.wallet_name ?? "N/A",
                    CategoryId = t.category_id,
                    // Thay đổi hiển thị nếu là Chuyển khoản
                    CategoryName = isTransfer ? "Chuyển khoản" : (category?.category_name ?? "Khác"),
                    CategoryIcon = isTransfer ? "bx bx-transfer-alt" : (category?.icon ?? "bx bx-category"),
                    CategoryColor = isTransfer ? "#696cff" : (category?.color ?? "#8592A3"),

                    TransactionType = t.transaction_type,
                    IsTransfer = isTransfer,
                    AmountValue = t.amount,
                    AmountText = $"{signedAmount.ToString("+#,##0;-#,##0;0", culture)} {currency}",
                    TransactionDate = t.transaction_date,
                    TransactionDateText = t.transaction_date.ToString("dd/MM/yyyy"),
                    Note = t.note ?? string.Empty,
                    RecurringBadgeText = t.is_recurring ? MapRecurringLabel(t.recur_interval) : "Một lần",
                    Currency = currency
                };
            })
            .OrderByDescending(x => x.TransactionDate)
            .ThenByDescending(x => x.TransactionId)
            .ToList();
        }

        private static void NormalizeCreateRequest(TransactionCreateRequestDto request)
        {
            request.wallet_id = request.wallet_id?.Trim() ?? string.Empty;
            request.category_id = request.category_id?.Trim() ?? string.Empty;
            request.transaction_type = request.transaction_type?.Trim().ToLowerInvariant() ?? string.Empty;
            request.note = NormalizeNote(request.note);
            var recurInterval = request.recur_interval;
            NormalizeRecurring(request.is_recurring, ref recurInterval);
            request.recur_interval = recurInterval;
        }

        private static void NormalizeUpdateRequest(TransactionUpdateRequestDto request)
        {
            request.wallet_id = request.wallet_id?.Trim();
            request.category_id = request.category_id?.Trim();
            request.transaction_type = request.transaction_type?.Trim().ToLowerInvariant();
            request.note = NormalizeNote(request.note);

            var recurringValue = request.recur_interval;
            NormalizeRecurring(request.is_recurring ?? false, ref recurringValue, request.is_recurring.HasValue);
            request.recur_interval = recurringValue;
        }

        private static string? ValidateCreateRequest(TransactionCreateRequestDto request)
        {
            if (string.IsNullOrWhiteSpace(request.wallet_id)) return "Vui lòng chọn ví.";
            if (string.IsNullOrWhiteSpace(request.category_id)) return "Vui lòng chọn danh mục.";
            if (request.transaction_type is not ("income" or "expense")) return "Loại giao dịch không hợp lệ.";
            if (request.amount <= 0) return "Số tiền phải lớn hơn 0.";
            if (request.transaction_date == default) return "Vui lòng chọn ngày giao dịch.";
            if (request.is_recurring && string.IsNullOrWhiteSpace(request.recur_interval)) return "Vui lòng chọn chu kỳ lặp.";
            if (!string.IsNullOrWhiteSpace(request.recur_interval) && !IsValidRecurring(request.recur_interval)) return "Chu kỳ lặp không hợp lệ.";
            return null;
        }

        private static string? ValidateUpdateRequest(TransactionUpdateRequestDto request)
        {
            if (request.wallet_id != null && string.IsNullOrWhiteSpace(request.wallet_id)) return "Ví không hợp lệ.";
            if (request.category_id != null && string.IsNullOrWhiteSpace(request.category_id)) return "Danh mục không hợp lệ.";
            if (request.transaction_type != null && request.transaction_type is not ("income" or "expense")) return "Loại giao dịch không hợp lệ.";
            if (request.amount.HasValue && request.amount <= 0) return "Số tiền phải lớn hơn 0.";
            if (request.is_recurring == true && string.IsNullOrWhiteSpace(request.recur_interval)) return "Vui lòng chọn chu kỳ lặp.";
            if (!string.IsNullOrWhiteSpace(request.recur_interval) && !IsValidRecurring(request.recur_interval)) return "Chu kỳ lặp không hợp lệ.";
            return null;
        }

        private static string? NormalizeNote(string? note)
        {
            var value = note?.Trim();
            return string.IsNullOrWhiteSpace(value) ? null : value;
        }

        private static void NormalizeRecurring(bool isRecurring, ref string? recurInterval, bool forceNullWhenNotRecurring = true)
        {
            recurInterval = recurInterval?.Trim().ToLowerInvariant();

            if (!isRecurring)
            {
                if (forceNullWhenNotRecurring)
                {
                    recurInterval = null;
                }
                return;
            }

            if (string.IsNullOrWhiteSpace(recurInterval))
            {
                recurInterval = null;
            }
        }

        private static bool IsValidRecurring(string? recurInterval)
        {
            return recurInterval is "daily" or "weekly" or "monthly" or "yearly";
        }

        private static string MapRecurringLabel(string? recurInterval)
        {
            return recurInterval?.Trim().ToLowerInvariant() switch
            {
                "daily" => "Hàng ngày",
                "weekly" => "Hàng tuần",
                "monthly" => "Hàng tháng",
                "yearly" => "Hàng năm",
                _ => "Định kỳ"
            };
        }

        private static string ExtractApiMessage(string rawMessage, string fallback)
        {
            if (string.IsNullOrWhiteSpace(rawMessage))
            {
                return fallback;
            }

            try
            {
                using var doc = JsonDocument.Parse(rawMessage);

                if (doc.RootElement.TryGetProperty("detail", out var detailElement) && detailElement.ValueKind == JsonValueKind.String)
                {
                    return detailElement.GetString() ?? fallback;
                }

                if (doc.RootElement.TryGetProperty("message", out var messageElement) && messageElement.ValueKind == JsonValueKind.String)
                {
                    return messageElement.GetString() ?? fallback;
                }
            }
            catch
            {
            }

            return rawMessage;
        }
    }
}