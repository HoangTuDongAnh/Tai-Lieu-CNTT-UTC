using System.Text.Json.Serialization;

namespace ExpenseWeb.Models.Dtos.Reports
{
    public class CashflowAnalyticsDto
    {
        [JsonPropertyName("start_date")]
        public DateTime start_date { get; set; }

        [JsonPropertyName("end_date")]
        public DateTime end_date { get; set; }

        [JsonPropertyName("granularity")]
        public string granularity { get; set; } = "day";

        [JsonPropertyName("total_income")]
        public decimal total_income { get; set; }

        [JsonPropertyName("total_expense")]
        public decimal total_expense { get; set; }

        [JsonPropertyName("net_change")]
        public decimal net_change { get; set; }

        [JsonPropertyName("transaction_count")]
        public int transaction_count { get; set; }

        [JsonPropertyName("average_expense")]
        public decimal average_expense { get; set; }

        [JsonPropertyName("busiest_label")]
        public string? busiest_label { get; set; }

        [JsonPropertyName("series")]
        public List<CashflowSeriesItemDto> series { get; set; } = new();
    }

    public class CashflowSeriesItemDto
    {
        [JsonPropertyName("key")]
        public string key { get; set; } = string.Empty;

        [JsonPropertyName("label")]
        public string label { get; set; } = string.Empty;

        [JsonPropertyName("income")]
        public decimal income { get; set; }

        [JsonPropertyName("expense")]
        public decimal expense { get; set; }

        [JsonPropertyName("net")]
        public decimal net { get; set; }

        [JsonPropertyName("running_balance")]
        public decimal running_balance { get; set; }
    }
}
