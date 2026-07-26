using System.Text.Json.Serialization;

namespace ExpenseWeb.Models.Dtos.Reports
{
    public class CategorySummaryItemDto
    {
        [JsonPropertyName("category_id")]
        public string category_id { get; set; } = string.Empty;

        [JsonPropertyName("category_name")]
        public string category_name { get; set; } = string.Empty;

        [JsonPropertyName("icon")]
        public string? icon { get; set; }

        [JsonPropertyName("color")]
        public string? color { get; set; }

        [JsonPropertyName("total_amount")]
        public decimal total_amount { get; set; }

        [JsonPropertyName("percentage")]
        public decimal percentage { get; set; }
    }
}
