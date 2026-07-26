using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using ExpenseWeb.Models.Dtos.Category;

namespace ExpenseWeb.Services.Api
{
    public class CategoryApiService
    {
        private readonly HttpClient _httpClient;
        private readonly string _baseUrl;
        private readonly JsonSerializerOptions _jsonOptions = new()
        {
            PropertyNameCaseInsensitive = true,
            NumberHandling = JsonNumberHandling.AllowReadingFromString
        };

        public CategoryApiService(HttpClient httpClient, IConfiguration configuration)
        {
            _httpClient = httpClient;
            _baseUrl = (configuration["ApiSettings:BaseUrl"] ?? string.Empty).TrimEnd('/');
        }

        private HttpRequestMessage CreateRequest(HttpMethod method, string url, string token, HttpContent? content = null)
        {
            var request = new HttpRequestMessage(method, url);
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            if (content != null) request.Content = content;
            return request;
        }

        private StringContent CreateJsonContent<T>(T payload) =>
            new(JsonSerializer.Serialize(payload), Encoding.UTF8, "application/json");

        public async Task<List<CategoryOverviewResponseDto>> GetOverviewAsync(string token, string periodType, int year, int? month, int? week)
        {
            var query = new List<string>
            {
                $"period_type={Uri.EscapeDataString(periodType)}",
                $"period_year={year}"
            };
            if (month.HasValue) query.Add($"period_month={month.Value}");
            if (week.HasValue) query.Add($"period_week={week.Value}");

            var request = CreateRequest(HttpMethod.Get, $"{_baseUrl}/categories/overview?{string.Join("&", query)}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<CategoryOverviewResponseDto>>(body, _jsonOptions) ?? new List<CategoryOverviewResponseDto>();
        }

        public async Task<List<CategoryResponseDto>> GetCategoriesAsync(string token, bool includeDeleted = false)
        {
            var url = includeDeleted
                ? $"{_baseUrl}/categories?include_deleted=true"
                : $"{_baseUrl}/categories";

            var request = CreateRequest(HttpMethod.Get, url, token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<CategoryResponseDto>>(body, _jsonOptions) ?? new List<CategoryResponseDto>();
        }

        public async Task<CategoryResponseDto?> CreateCategoryAsync(string token, CategoryCreateRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Post, $"{_baseUrl}/categories", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<CategoryResponseDto>(body, _jsonOptions);
        }

        public async Task<CategoryResponseDto?> UpdateCategoryAsync(string token, string categoryId, CategoryUpdateRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Put, $"{_baseUrl}/categories/{categoryId}", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<CategoryResponseDto>(body, _jsonOptions);
        }

        public async Task DeleteCategoryAsync(string token, string categoryId, CategoryDeleteRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Delete, $"{_baseUrl}/categories/{categoryId}", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);
        }

        public async Task<List<BudgetResponseDto>> GetBudgetsAsync(string token, string? periodType = null, int? year = null, int? month = null, int? week = null, string? categoryId = null)
        {
            var query = new List<string>();
            if (!string.IsNullOrWhiteSpace(periodType)) query.Add($"period_type={Uri.EscapeDataString(periodType)}");
            if (year.HasValue) query.Add($"period_year={year.Value}");
            if (month.HasValue) query.Add($"period_month={month.Value}");
            if (week.HasValue) query.Add($"period_week={week.Value}");
            if (!string.IsNullOrWhiteSpace(categoryId)) query.Add($"category_id={Uri.EscapeDataString(categoryId)}");

            var url = query.Count > 0
                ? $"{_baseUrl}/budgets?{string.Join("&", query)}"
                : $"{_baseUrl}/budgets";

            var request = CreateRequest(HttpMethod.Get, url, token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<BudgetResponseDto>>(body, _jsonOptions) ?? new List<BudgetResponseDto>();
        }

        public async Task<BudgetResponseDto?> CreateBudgetAsync(string token, BudgetCreateRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Post, $"{_baseUrl}/budgets", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<BudgetResponseDto>(body, _jsonOptions);
        }

        public async Task<BudgetResponseDto?> UpdateBudgetAsync(string token, string budgetId, BudgetUpdateRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Put, $"{_baseUrl}/budgets/{budgetId}", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<BudgetResponseDto>(body, _jsonOptions);
        }

        public async Task DeleteBudgetAsync(string token, string budgetId)
        {
            var request = CreateRequest(HttpMethod.Delete, $"{_baseUrl}/budgets/{budgetId}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);
        }

    }
}