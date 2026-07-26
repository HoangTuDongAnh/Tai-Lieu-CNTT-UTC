using System.Net;
using System.Net.Http.Headers;
using System.Text.Json;
using ExpenseWeb.Models.Dtos.Admin;

namespace ExpenseWeb.Services.Api
{
    public class AdminApiService
    {
        private readonly HttpClient _httpClient;
        private readonly string _baseUrl;
        private readonly JsonSerializerOptions _jsonOptions = new()
        {
            PropertyNameCaseInsensitive = true
        };

        public AdminApiService(HttpClient httpClient, IConfiguration configuration)
        {
            _httpClient = httpClient;
            _baseUrl = (configuration["ApiSettings:BaseUrl"] ?? string.Empty).TrimEnd('/');
        }

        private HttpRequestMessage CreateRequest(HttpMethod method, string url, string token)
        {
            var request = new HttpRequestMessage(method, url);
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            return request;
        }

        public async Task<(bool Success, string ErrorMessage, AdminDashboardDto? Data, HttpStatusCode StatusCode)> GetDashboardAsync(string token)
        {
            var request = CreateRequest(HttpMethod.Get, $"{_baseUrl}/admin/dashboard", token);
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), null, response.StatusCode);

            var data = JsonSerializer.Deserialize<AdminDashboardDto>(responseBody, _jsonOptions);
            return (true, string.Empty, data, response.StatusCode);
        }

        private string ExtractErrorMessage(string responseBody)
        {
            try
            {
                var doc = JsonDocument.Parse(responseBody);
                if (doc.RootElement.TryGetProperty("detail", out var detail))
                    return detail.ValueKind == JsonValueKind.String ? detail.GetString() ?? "Unknown error" : detail.ToString();

                if (doc.RootElement.TryGetProperty("message", out var message))
                    return message.ValueKind == JsonValueKind.String ? message.GetString() ?? "Unknown error" : message.ToString();
            }
            catch { }

            return string.IsNullOrWhiteSpace(responseBody) ? "Unknown error" : responseBody;
        }
    }
}
