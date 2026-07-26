using System.Text.Json.Serialization;

namespace ExpenseWeb.Models.Dtos.Reports
{
    public class WalletSummaryItemDto
    {
        [JsonPropertyName("wallet_id")]
        public string wallet_id { get; set; } = string.Empty;

        [JsonPropertyName("wallet_name")]
        public string wallet_name { get; set; } = string.Empty;

        [JsonPropertyName("currency")]
        public string currency { get; set; } = "VND";

        [JsonPropertyName("total_income")]
        public decimal total_income { get; set; }

        [JsonPropertyName("total_expense")]
        public decimal total_expense { get; set; }

        [JsonPropertyName("transaction_count")]
        public int transaction_count { get; set; }
    }
}
