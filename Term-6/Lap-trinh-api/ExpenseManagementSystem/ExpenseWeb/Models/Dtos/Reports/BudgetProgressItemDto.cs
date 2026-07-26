using System.Text.Json.Serialization;

namespace ExpenseWeb.Models.Dtos.Reports
{
    public class BudgetProgressItemDto
    {
        [JsonPropertyName("budget_id")]
        public string budget_id { get; set; } = string.Empty;

        [JsonPropertyName("category_id")]
        public string category_id { get; set; } = string.Empty;

        [JsonPropertyName("category_name")]
        public string category_name { get; set; } = string.Empty;

        [JsonPropertyName("category_icon")]
        public string? category_icon { get; set; }

        [JsonPropertyName("category_color")]
        public string? category_color { get; set; }

        [JsonPropertyName("limit_amount")]
        public decimal limit_amount { get; set; }

        [JsonPropertyName("spent_amount")]
        public decimal spent_amount { get; set; }

        [JsonPropertyName("remaining_amount")]
        public decimal remaining_amount { get; set; }

        [JsonPropertyName("percentage_used")]
        public decimal percentage_used { get; set; }

        [JsonPropertyName("status")]
        public string status { get; set; } = string.Empty;
    }
}
