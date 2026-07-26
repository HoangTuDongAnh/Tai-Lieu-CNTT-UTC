using System.Text.Json.Serialization;

namespace ExpenseWeb.Models.Dtos.Reports
{
    public class TopExpenseItemDto
    {
        [JsonPropertyName("transaction_id")]
        public string transaction_id { get; set; } = string.Empty;

        [JsonPropertyName("transaction_date")]
        public DateTime transaction_date { get; set; }

        [JsonPropertyName("amount")]
        public decimal amount { get; set; }

        [JsonPropertyName("note")]
        public string? note { get; set; }

        [JsonPropertyName("wallet_id")]
        public string wallet_id { get; set; } = string.Empty;

        [JsonPropertyName("wallet_name")]
        public string wallet_name { get; set; } = string.Empty;

        [JsonPropertyName("category_id")]
        public string category_id { get; set; } = string.Empty;

        [JsonPropertyName("category_name")]
        public string category_name { get; set; } = string.Empty;

        [JsonPropertyName("category_icon")]
        public string? category_icon { get; set; }

        [JsonPropertyName("category_color")]
        public string? category_color { get; set; }
    }
}
