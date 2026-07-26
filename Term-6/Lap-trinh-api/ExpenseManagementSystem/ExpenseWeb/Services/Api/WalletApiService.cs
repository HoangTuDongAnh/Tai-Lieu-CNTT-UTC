using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using ExpenseWeb.Models.Dtos.Wallet;

namespace ExpenseWeb.Services.Api
{
    public class WalletApiService
    {
        private readonly HttpClient _httpClient;
        private readonly JsonSerializerOptions _jsonOptions = new()
        {
            PropertyNameCaseInsensitive = true,
            NumberHandling = JsonNumberHandling.AllowReadingFromString
        };

        public WalletApiService(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        private HttpRequestMessage CreateRequest(HttpMethod method, string url, string token, HttpContent? content = null)
        {
            var request = new HttpRequestMessage(method, url);
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            if (content != null) request.Content = content;
            return request;
        }

        private StringContent CreateJsonContent<T>(T data) =>
            new(JsonSerializer.Serialize(data, _jsonOptions), Encoding.UTF8, "application/json");

        public async Task<List<WalletResponseDto>> GetWalletsAsync(string token)
        {
            var request = CreateRequest(HttpMethod.Get, "/wallets", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<WalletResponseDto>>(body, _jsonOptions)
                   ?? new List<WalletResponseDto>();
        }

        public async Task<WalletResponseDto> GetWalletByIdAsync(string token, string walletId)
        {
            var request = CreateRequest(HttpMethod.Get, $"/wallets/{walletId}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<WalletResponseDto>(body, _jsonOptions)
                   ?? throw new InvalidOperationException("Không đọc được dữ liệu ví.");
        }

        public async Task<WalletResponseDto> CreateWalletAsync(string token, WalletCreateRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Post, "/wallets", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<WalletResponseDto>(body, _jsonOptions)
                   ?? throw new InvalidOperationException("API tạo ví trả về rỗng.");
        }

        public async Task<WalletResponseDto> UpdateWalletAsync(string token, string walletId, WalletUpdateRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Put, $"/wallets/{walletId}", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<WalletResponseDto>(body, _jsonOptions)
                   ?? throw new InvalidOperationException("API cập nhật ví trả về rỗng.");
        }

        public async Task DeleteWalletAsync(string token, string walletId, WalletDeleteRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Delete, $"/wallets/{walletId}", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);
        }
    }
}