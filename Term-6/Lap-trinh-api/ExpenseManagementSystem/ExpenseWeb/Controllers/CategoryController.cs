using System.Globalization;
using System.Text.Json;
using ExpenseWeb.Models.Dtos.Category;
using ExpenseWeb.Models.ViewModels.Category;
using ExpenseWeb.Services.Api;
using Microsoft.AspNetCore.Mvc;
using ExpenseWeb.Filters;

namespace ExpenseWeb.Controllers
{
    [RequireRole("user")]
    public class CategoryController : Controller
    {
        private readonly CategoryApiService _categoryApiService;

        public CategoryController(CategoryApiService categoryApiService)
        {
            _categoryApiService = categoryApiService;
        }

        [HttpGet]
        public async Task<IActionResult> Index(string? periodType, int? year, int? month, int? week, string? lang)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrEmpty(token))
            {
                return RedirectToAction("Login", "Auth");
            }

            ViewBag.UserFullName = HttpContext.Session.GetString("UserFullName");

            var now = DateTime.Now;
            var resolvedPeriodType = string.IsNullOrWhiteSpace(periodType) ? "month" : periodType.Trim().ToLowerInvariant();
            var resolvedYear = year ?? now.Year;
            int? resolvedMonth = resolvedPeriodType == "month" ? (month ?? now.Month) : (int?)null;
            int? resolvedWeek = resolvedPeriodType == "week" ? (week ?? ISOWeek.GetWeekOfYear(now)) : (int?)null;
            var resolvedLang = string.IsNullOrWhiteSpace(lang) ? "vi" : lang.Trim().ToLowerInvariant();

            var viewModel = new CategoryPageViewModel
            {
                Lang = resolvedLang,
                CurrentPeriodType = resolvedPeriodType,
                CurrentYear = resolvedYear,
                CurrentMonth = resolvedMonth,
                CurrentWeek = resolvedWeek
            };

            try
            {
                viewModel.Categories = SortCategories(await _categoryApiService.GetOverviewAsync(
                    token,
                    resolvedPeriodType,
                    resolvedYear,
                    resolvedMonth,
                    resolvedWeek
                ));
            }
            catch (Exception ex)
            {
                viewModel.Categories = new List<CategoryOverviewResponseDto>();
                TempData["CategoryPageError"] = ExtractApiMessage(ex.Message, "Không tải được dữ liệu danh mục từ API.");
            }

            return View(viewModel);
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] CategoryCreateRequestDto request)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrEmpty(token))
            {
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });
            }

            try
            {
                request.category_type = NormalizeCategoryType(request.category_type);
                await _categoryApiService.CreateCategoryAsync(token, request);
                return Json(new { success = true, message = "Tạo danh mục thành công." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ExtractApiMessage(ex.Message, "Tạo danh mục thất bại.") });
            }
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> Update(string id, [FromBody] CategoryUpdateRequestDto request)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrEmpty(token))
            {
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });
            }

            try
            {
                request.category_type = NormalizeCategoryType(request.category_type);
                await _categoryApiService.UpdateCategoryAsync(token, id, request);
                return Json(new { success = true, message = "Cập nhật danh mục thành công." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ExtractApiMessage(ex.Message, "Cập nhật danh mục thất bại.") });
            }
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(string id, [FromBody] CategoryDeleteRequestDto? request)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrEmpty(token))
            {
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });
            }

            try
            {
                await _categoryApiService.DeleteCategoryAsync(token, id, request ?? new CategoryDeleteRequestDto());

                return Json(new
                {
                    success = true,
                    message = "Xóa danh mục và toàn bộ hạn mức, giao dịch liên quan thành công."
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ExtractApiMessage(ex.Message, "Xóa danh mục thất bại.") });
            }
        }

        public class SaveBudgetWebRequest
        {
            public string category_id { get; set; } = string.Empty;
            public string period_type { get; set; } = "month";
            public int? period_year { get; set; }
            public int? period_month { get; set; }
            public int? period_week { get; set; }
            public decimal? limit_amount { get; set; }
            public string? budget_id { get; set; }
        }

        [HttpPost]
        public async Task<IActionResult> SaveBudget([FromBody] SaveBudgetWebRequest request)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrEmpty(token)) return Unauthorized(new { success = false, message = "Hết hạn phiên." });

            try
            {
                var category = (await _categoryApiService.GetCategoriesAsync(token)).FirstOrDefault(x => x.category_id == request.category_id);
                if (category != null && string.Equals(category.category_type, "income", StringComparison.OrdinalIgnoreCase))
                {
                    return BadRequest(new { success = false, message = "Danh mục thu nhập không thể thiết lập hạn mức." });
                }

                var now = DateTime.Now;
                var periodType = request.period_type?.Trim().ToLower() ?? "month";
                var year = request.period_year ?? now.Year;
                int? month = periodType == "month" ? request.period_month ?? now.Month : null;
                int? week = periodType == "week" ? request.period_week ?? ISOWeek.GetWeekOfYear(now) : null;

                if (!string.IsNullOrWhiteSpace(request.budget_id))
                {
                    await _categoryApiService.UpdateBudgetAsync(token, request.budget_id, new BudgetUpdateRequestDto
                    {
                        limit_amount = request.limit_amount,
                        period_type = periodType,
                        period_year = year,
                        period_month = month,
                        period_week = week
                    });
                }
                else
                {
                    var budgets = await _categoryApiService.GetBudgetsAsync(token, periodType, year, month, week, request.category_id);
                    var existing = budgets.FirstOrDefault();

                    if (existing != null)
                    {
                        await _categoryApiService.UpdateBudgetAsync(token, existing.budget_id, new BudgetUpdateRequestDto
                        {
                            limit_amount = request.limit_amount,
                            period_type = periodType,
                            period_year = year,
                            period_month = month,
                            period_week = week
                        });
                    }
                    else
                    {
                        await _categoryApiService.CreateBudgetAsync(token, new BudgetCreateRequestDto
                        {
                            category_id = request.category_id,
                            limit_amount = request.limit_amount ?? 0,
                            period_type = periodType,
                            period_year = year,
                            period_month = month,
                            period_week = week
                        });
                    }
                }

                return Json(new { success = true, message = "Lưu hạn mức thành công.", shouldReload = true });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }


        [HttpPost]
        public async Task<IActionResult> DeleteBudgetsByCategory([FromBody] DeleteBudgetsByCategoryRequest request)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrEmpty(token))
            {
                return Unauthorized(new { success = false, message = "Phiên đăng nhập đã hết hạn." });
            }

            if (request == null || string.IsNullOrWhiteSpace(request.category_id))
            {
                return BadRequest(new { success = false, message = "Thiếu category_id." });
            }

            try
            {
                var budgets = await _categoryApiService.GetBudgetsAsync(token, categoryId: request.category_id);
                foreach (var budget in budgets.Where(x => !string.IsNullOrWhiteSpace(x.budget_id)))
                {
                    await _categoryApiService.DeleteBudgetAsync(token, budget.budget_id);
                }

                return Json(new { success = true, deleted_count = budgets.Count, message = "Đã xóa toàn bộ hạn mức của danh mục." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ExtractApiMessage(ex.Message, "Xóa hạn mức của danh mục thất bại.") });
            }
        }

        public class DeleteBudgetsByCategoryRequest
        {
            public string category_id { get; set; } = string.Empty;
        }

        [HttpGet]
        public async Task<IActionResult> BudgetsByCategory(string categoryId)
        {
            var token = HttpContext.Session.GetString("AccessToken");
            if (string.IsNullOrEmpty(token)) return Unauthorized(new { success = false, message = "Hết hạn phiên." });

            try
            {
                var budgets = await _categoryApiService.GetBudgetsAsync(token, categoryId: categoryId);
                return Json(new { success = true, items = budgets.OrderByDescending(x => x.period_year).ThenByDescending(x => x.period_month ?? 0).ThenByDescending(x => x.period_week ?? 0).ToList() });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ExtractApiMessage(ex.Message, "Không tải được danh sách hạn mức.") });
            }
        }



        private static List<CategoryOverviewResponseDto> SortCategories(IEnumerable<CategoryOverviewResponseDto>? categories)
        {
            var expenseDefaultOrder = new Dictionary<string, int>(StringComparer.OrdinalIgnoreCase)
            {
                ["Ăn uống"] = 0,
                ["Đi lại"] = 1,
                ["Hóa đơn"] = 2,
                ["Mua sắm"] = 3,
                ["Sức khỏe"] = 4,
                ["Giáo dục"] = 5,
                ["Giải trí"] = 6,
                ["Du lịch"] = 7,
                ["Tiết kiệm"] = 8,
                ["Khác"] = 9
            };

            var incomeDefaultOrder = new Dictionary<string, int>(StringComparer.OrdinalIgnoreCase)
            {
                ["Lương"] = 0,
                ["Thưởng"] = 1,
                ["Đầu tư"] = 2,
                ["Quà tặng"] = 3,
                ["Khác"] = 4
            };

            return (categories ?? Enumerable.Empty<CategoryOverviewResponseDto>())
                .Select((item, index) => new { item, index })
                .OrderBy(x => GetCategoryTypeOrder(x.item.category_type))
                .ThenBy(x => x.item.is_default ? 0 : 1)
                .ThenBy(x => GetDefaultCategoryOrder(x.item, expenseDefaultOrder, incomeDefaultOrder))
                .ThenBy(x => x.item.is_default ? x.index : x.index)
                .ToList()
                .Select(x => x.item)
                .ToList();
        }

        private static int GetCategoryTypeOrder(string? categoryType)
        {
            return string.Equals(categoryType, "income", StringComparison.OrdinalIgnoreCase) ? 1 : 0;
        }

        private static int GetDefaultCategoryOrder(
            CategoryOverviewResponseDto item,
            IReadOnlyDictionary<string, int> expenseDefaultOrder,
            IReadOnlyDictionary<string, int> incomeDefaultOrder)
        {
            if (!item.is_default)
            {
                return int.MaxValue;
            }

            var categoryName = item.category_name?.Trim() ?? string.Empty;
            if (string.Equals(item.category_type, "income", StringComparison.OrdinalIgnoreCase))
            {
                return incomeDefaultOrder.TryGetValue(categoryName, out var incomeOrder) ? incomeOrder : 999;
            }

            return expenseDefaultOrder.TryGetValue(categoryName, out var expenseOrder) ? expenseOrder : 999;
        }

        private static string NormalizeCategoryType(string? value)
        {
            return string.Equals(value, "income", StringComparison.OrdinalIgnoreCase) ? "income" : "expense";
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

                if (doc.RootElement.TryGetProperty("detail", out var detailElement))
                {
                    if (detailElement.ValueKind == JsonValueKind.String)
                    {
                        return detailElement.GetString() ?? fallback;
                    }

                    if (detailElement.ValueKind == JsonValueKind.Array && detailElement.GetArrayLength() > 0)
                    {
                        var first = detailElement[0];

                        if (first.ValueKind == JsonValueKind.Object && first.TryGetProperty("msg", out var msg))
                        {
                            return msg.GetString() ?? fallback;
                        }

                        return detailElement.ToString();
                    }
                }

                if (doc.RootElement.TryGetProperty("message", out var messageElement))
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