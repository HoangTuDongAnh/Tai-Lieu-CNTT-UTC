using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using ExpenseWeb.Models.Dtos.Transaction;

namespace ExpenseWeb.Services.Api
{
    public class TransactionApiService
    {
        private readonly HttpClient _httpClient;
        private readonly string _baseUrl;
        private readonly JsonSerializerOptions _jsonOptions = new()
        {
            PropertyNameCaseInsensitive = true,
            NumberHandling = JsonNumberHandling.AllowReadingFromString
        };

        public TransactionApiService(HttpClient httpClient, IConfiguration configuration)
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

        public async Task<List<TransactionResponseDto>> GetTransactionsAsync(string token)
        {
            var request = CreateRequest(HttpMethod.Get, $"{_baseUrl}/transactions", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<List<TransactionResponseDto>>(body, _jsonOptions) ?? new List<TransactionResponseDto>();
        }

        public async Task<TransactionResponseDto?> CreateTransactionAsync(string token, TransactionCreateRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Post, $"{_baseUrl}/transactions", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<TransactionResponseDto>(body, _jsonOptions);
        }

        public async Task<TransactionResponseDto?> UpdateTransactionAsync(string token, string transactionId, TransactionUpdateRequestDto dto)
        {
            var request = CreateRequest(HttpMethod.Put, $"{_baseUrl}/transactions/{transactionId}", token, CreateJsonContent(dto));
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);

            return JsonSerializer.Deserialize<TransactionResponseDto>(body, _jsonOptions);
        }

        public async Task DeleteTransactionAsync(string token, string transactionId)
        {
            var request = CreateRequest(HttpMethod.Delete, $"{_baseUrl}/transactions/{transactionId}", token);
            var response = await _httpClient.SendAsync(request);
            var body = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new InvalidOperationException(body);
        }

        public async Task<TransferResponseDto> TransferAsync(string token, TransferCreateRequestDto request)
        {
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

            var response = await _httpClient.PostAsJsonAsync("transactions/transfer", request);

            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                throw new Exception(error);
            }

            return await response.Content.ReadFromJsonAsync<TransferResponseDto>()
                   ?? throw new Exception("Không thể đọc dữ liệu trả về từ API.");
        }
    }
}