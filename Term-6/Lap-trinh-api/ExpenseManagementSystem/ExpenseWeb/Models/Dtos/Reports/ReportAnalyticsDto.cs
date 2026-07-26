using System.Text.Json.Serialization;

namespace ExpenseWeb.Models.Dtos.Reports
{
    public class ReportAnalyticsDto
    {
        [JsonPropertyName("date_from")]
        public DateTime date_from { get; set; }

        [JsonPropertyName("date_to")]
        public DateTime date_to { get; set; }

        [JsonPropertyName("group_by")]
        public string group_by { get; set; } = "day";

        [JsonPropertyName("summary")]
        public ReportSummaryDto summary { get; set; } = new();

        [JsonPropertyName("account_balance")]
        public List<ReportSeriesPointDto> account_balance { get; set; } = new();

        [JsonPropertyName("changes")]
        public List<ReportSeriesPointDto> changes { get; set; } = new();

        [JsonPropertyName("category_breakdown")]
        public List<CategorySummaryItemDto> category_breakdown { get; set; } = new();

        [JsonPropertyName("budget_progress")]
        public List<BudgetProgressItemDto> budget_progress { get; set; } = new();
    }

    public class ReportSummaryDto
    {
        [JsonPropertyName("current_wallet_balance")]
        public decimal current_wallet_balance { get; set; }

        [JsonPropertyName("total_period_change")]
        public decimal total_period_change { get; set; }

        [JsonPropertyName("total_period_expenses")]
        public decimal total_period_expenses { get; set; }

        [JsonPropertyName("total_period_income")]
        public decimal total_period_income { get; set; }

        [JsonPropertyName("total_transactions")]
        public int total_transactions { get; set; }
    }

    public class ReportSeriesPointDto
    {
        [JsonPropertyName("label")]
        public string label { get; set; } = string.Empty;

        [JsonPropertyName("income")]
        public decimal income { get; set; }

        [JsonPropertyName("expense")]
        public decimal expense { get; set; }

        [JsonPropertyName("net")]
        public decimal net { get; set; }

        [JsonPropertyName("balance")]
        public decimal balance { get; set; }
    }
}
