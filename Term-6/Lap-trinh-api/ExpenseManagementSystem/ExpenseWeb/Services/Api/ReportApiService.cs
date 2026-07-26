using System.Net.Http.Headers;
using System.Text.Json;
using System.Text.Json.Serialization;
using ExpenseWeb.Models.Dtos.Reports;

namespace ExpenseWeb.Services.Api
{
    public class ReportApiService
    {
        private readonly HttpClient _httpClient;
        private readonly JsonSerializerOptions _jsonOptions = new()
        {
            PropertyNameCaseInsensitive = true,
            NumberHandling = JsonNumberHandling.AllowReadingFromString
        };

        public ReportApiService(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        private HttpRequestMessage CreateRequest(string url, string token) =>
            new(HttpMethod.Get, url)
            {
                Headers = { Authorization = new AuthenticationHeaderValue("Bearer", token) }
            };

        public async Task<DashboardOverviewDto> GetDashboardOverviewAsync(string token)
        {
            var request = CreateRequest("/reports/dashboard", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<DashboardOverviewDto>(body, _jsonOptions)
                   ?? throw new InvalidOperationException("Dashboard overview response is empty.");
        }

        public async Task<DashboardOverviewDto> GetDashboardOverviewRangeAsync(string token, DateTime startDate, DateTime endDate)
        {
            var request = CreateRequest($"/reports/dashboard-range?start_date={startDate:yyyy-MM-dd}&end_date={endDate:yyyy-MM-dd}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<DashboardOverviewDto>(body, _jsonOptions)
                   ?? throw new InvalidOperationException("Dashboard range overview response is empty.");
        }

        public async Task<List<MonthlySummaryItemDto>> GetMonthlySummaryAsync(string token, int year)
        {
            var request = CreateRequest($"/reports/monthly?year={year}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<MonthlySummaryItemDto>>(body, _jsonOptions) ?? new();
        }

        public async Task<List<CategorySummaryItemDto>> GetCategorySummaryAsync(string token, int month, int year)
        {
            var request = CreateRequest($"/reports/by-category?month={month}&year={year}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<CategorySummaryItemDto>>(body, _jsonOptions) ?? new();
        }

        public async Task<List<CategorySummaryItemDto>> GetCategorySummaryRangeAsync(string token, DateTime startDate, DateTime endDate)
        {
            var request = CreateRequest($"/reports/by-category-range?start_date={startDate:yyyy-MM-dd}&end_date={endDate:yyyy-MM-dd}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<CategorySummaryItemDto>>(body, _jsonOptions) ?? new();
        }

        public async Task<List<TopExpenseItemDto>> GetTopExpensesAsync(string token, int month, int year, int limit = 5)
        {
            var request = CreateRequest($"/reports/top-expenses?month={month}&year={year}&limit={limit}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<TopExpenseItemDto>>(body, _jsonOptions) ?? new();
        }

        public async Task<List<TopExpenseItemDto>> GetTopExpensesRangeAsync(string token, DateTime startDate, DateTime endDate, int limit = 5)
        {
            var request = CreateRequest($"/reports/top-expenses-range?start_date={startDate:yyyy-MM-dd}&end_date={endDate:yyyy-MM-dd}&limit={limit}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<TopExpenseItemDto>>(body, _jsonOptions) ?? new();
        }

        public async Task<List<BudgetProgressItemDto>> GetBudgetProgressAsync(string token, int month, int year)
        {
            var request = CreateRequest($"/reports/budget-progress?month={month}&year={year}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<BudgetProgressItemDto>>(body, _jsonOptions) ?? new();
        }

        public async Task<CashflowAnalyticsDto> GetCashflowAnalyticsAsync(string token, DateTime startDate, DateTime endDate, string granularity)
        {
            var request = CreateRequest(
                $"/reports/cashflow-analytics?start_date={startDate:yyyy-MM-dd}&end_date={endDate:yyyy-MM-dd}&granularity={granularity}",
                token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<CashflowAnalyticsDto>(body, _jsonOptions)
                   ?? throw new InvalidOperationException("Cashflow analytics response is empty.");
        }
    }
}