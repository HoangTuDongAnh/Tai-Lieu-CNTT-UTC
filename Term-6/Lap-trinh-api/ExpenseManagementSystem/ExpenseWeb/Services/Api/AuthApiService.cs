using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using ExpenseWeb.Models.Dtos.Auth;

namespace ExpenseWeb.Services.Api
{
    public class AuthApiService
    {
        private readonly HttpClient _httpClient;
        private readonly string _baseUrl;
        private readonly JsonSerializerOptions _jsonOptions = new()
        {
            PropertyNameCaseInsensitive = true
        };

        public AuthApiService(HttpClient httpClient, IConfiguration configuration)
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

        public async Task<(bool Success, string ErrorMessage, LoginResponseDto? Data)> LoginAsync(LoginRequestDto request)
        {
            var response = await _httpClient.PostAsync($"{_baseUrl}/auth/login", CreateJsonContent(request));
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), null);

            var data = JsonSerializer.Deserialize<LoginResponseDto>(responseBody, _jsonOptions);
            return (true, "", data);
        }

        public async Task<(bool Success, string ErrorMessage)> ForgotPasswordAsync(string email)
        {
            var response = await _httpClient.PostAsync($"{_baseUrl}/auth/forgot-password", CreateJsonContent(new { email }));
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody));
            return (true, "");
        }

        public async Task<(bool Success, string ErrorMessage)> ResetPasswordAsync(string token, string newPassword)
        {
            var response = await _httpClient.PostAsync($"{_baseUrl}/auth/reset-password", CreateJsonContent(new
            {
                token,
                new_password = newPassword
            }));
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody));
            return (true, "");
        }

        public async Task<(bool Success, string ErrorMessage, RegisterResponseDto? Data)> RegisterAsync(RegisterRequestDto request)
        {
            var response = await _httpClient.PostAsync($"{_baseUrl}/auth/register", CreateJsonContent(request));
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), null);

            var data = JsonSerializer.Deserialize<RegisterResponseDto>(responseBody, _jsonOptions);
            return (true, "", data);
        }

        public async Task<(bool Success, string ErrorMessage)> VerifyOtpAsync(string email, string otp)
        {
            var response = await _httpClient.PostAsync($"{_baseUrl}/auth/verify-otp", CreateJsonContent(new { email, otp }));
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody));
            return (true, "");
        }

        public async Task<(bool Success, string ErrorMessage)> ResendOtpAsync(string email)
        {
            var response = await _httpClient.PostAsync($"{_baseUrl}/auth/resend-otp", CreateJsonContent(new { email }));
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody));

            try
            {
                var doc = JsonDocument.Parse(responseBody);
                if (doc.RootElement.TryGetProperty("message", out var message))
                    return (true, message.GetString() ?? "");
            }
            catch { }

            return (true, "");
        }

        public async Task<(bool Success, string ErrorMessage, UserDto? Data)> GetMeAsync(string token)
        {
            var request = CreateRequest(HttpMethod.Get, $"{_baseUrl}/auth/me", token);
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), null);

            var data = JsonSerializer.Deserialize<UserDto>(responseBody, _jsonOptions);
            return (true, "", data);
        }

        public async Task<(bool Success, string ErrorMessage, UserDto? Data)> UpdateProfileAsync(string token, UpdateProfileRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Put, $"{_baseUrl}/auth/me", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody), null);

            var data = JsonSerializer.Deserialize<UserDto>(responseBody, _jsonOptions);
            return (true, "", data);
        }

        public async Task<(bool Success, string ErrorMessage)> DeleteMeAsync(string token)
        {
            var request = CreateRequest(HttpMethod.Delete, $"{_baseUrl}/auth/me", token);
            var response = await _httpClient.SendAsync(request);
            var responseBody = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                return (false, ExtractErrorMessage(responseBody));
            return (true, "");
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