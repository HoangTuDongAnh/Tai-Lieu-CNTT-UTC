using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using ExpenseWeb.Models.Dtos.Support;

namespace ExpenseWeb.Services.Api
{
    public class SupportApiService
    {
        private readonly HttpClient _httpClient;
        private readonly string _baseUrl;
        private readonly JsonSerializerOptions _jsonOptions = new()
        {
            PropertyNameCaseInsensitive = true
        };

        public SupportApiService(HttpClient httpClient, IConfiguration configuration)
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

        private StringContent CreateJsonContent<T>(T payload)
        {
            var json = JsonSerializer.Serialize(payload);
            return new StringContent(json, Encoding.UTF8, "application/json");
        }

        public async Task<(bool Success, string ErrorMessage, List<SupportRequestListItemDto> Data)> GetMyRequestsAsync(string token)
        {
            var request = CreateRequest(HttpMethod.Get, $"{_baseUrl}/support-requests/my", token);
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), new List<SupportRequestListItemDto>());

            var data = JsonSerializer.Deserialize<List<SupportRequestListItemDto>>(responseBody, _jsonOptions)
                       ?? new List<SupportRequestListItemDto>();
            return (true, string.Empty, data);
        }

        public async Task<(bool Success, string ErrorMessage, SupportRequestDetailDto? Data)> GetMyRequestDetailAsync(string token, string supportRequestId)
        {
            var request = CreateRequest(HttpMethod.Get, $"{_baseUrl}/support-requests/my/{supportRequestId}", token);
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), null);

            var data = JsonSerializer.Deserialize<SupportRequestDetailDto>(responseBody, _jsonOptions);
            return (true, string.Empty, data);
        }

        public async Task<(bool Success, string ErrorMessage, SupportRequestDetailDto? Data)> CreateRequestAsync(string token, SupportRequestCreateRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Post, $"{_baseUrl}/support-requests", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), null);

            var data = JsonSerializer.Deserialize<SupportRequestDetailDto>(responseBody, _jsonOptions);
            return (true, string.Empty, data);
        }

        public async Task<(bool Success, string ErrorMessage, List<SupportRequestListItemDto> Data)> GetAdminRequestsAsync(string token, SupportRequestAdminQueryDto query)
        {
            var parameters = new List<string>();
            if (!string.IsNullOrWhiteSpace(query.status)) parameters.Add($"status={Uri.EscapeDataString(query.status)}");
            if (!string.IsNullOrWhiteSpace(query.support_type)) parameters.Add($"support_type={Uri.EscapeDataString(query.support_type)}");
            if (!string.IsNullOrWhiteSpace(query.priority)) parameters.Add($"priority={Uri.EscapeDataString(query.priority)}");
            if (!string.IsNullOrWhiteSpace(query.keyword)) parameters.Add($"keyword={Uri.EscapeDataString(query.keyword)}");
            parameters.Add($"page={query.page}");
            parameters.Add($"page_size={query.page_size}");
            var qs = string.Join("&", parameters);

            var request = CreateRequest(HttpMethod.Get, $"{_baseUrl}/admin/support-requests?{qs}", token);
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), new List<SupportRequestListItemDto>());

            var data = JsonSerializer.Deserialize<List<SupportRequestListItemDto>>(responseBody, _jsonOptions)
                       ?? new List<SupportRequestListItemDto>();
            return (true, string.Empty, data);
        }

        public async Task<(bool Success, string ErrorMessage, SupportRequestDetailDto? Data)> GetAdminRequestDetailAsync(string token, string supportRequestId)
        {
            var request = CreateRequest(HttpMethod.Get, $"{_baseUrl}/admin/support-requests/{supportRequestId}", token);
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), null);

            var data = JsonSerializer.Deserialize<SupportRequestDetailDto>(responseBody, _jsonOptions);
            return (true, string.Empty, data);
        }

        public async Task<(bool Success, string ErrorMessage, SupportRequestDetailDto? Data)> ReplyAsync(string token, string supportRequestId, SupportRequestAdminReplyRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Patch, $"{_baseUrl}/admin/support-requests/{supportRequestId}/reply", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), null);

            var data = JsonSerializer.Deserialize<SupportRequestDetailDto>(responseBody, _jsonOptions);
            return (true, string.Empty, data);
        }

        private string ExtractErrorMessage(string responseBody)
        {
            try
            {
                var doc = JsonDocument.Parse(responseBody);
                if (doc.RootElement.TryGetProperty("detail", out var detail))
                {
                    return detail.ValueKind == JsonValueKind.String ? detail.GetString() ?? "Unknown error" : detail.ToString();
                }
            }
            catch
            {
            }

            return string.IsNullOrWhiteSpace(responseBody) ? "Unknown error" : responseBody;
        }
    }
}