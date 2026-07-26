using System.Text.Json.Serialization;

namespace ExpenseWeb.Models.Dtos.Reports
{
    public class MonthlySummaryItemDto
    {
        [JsonPropertyName("month")]
        public int month { get; set; }

        [JsonPropertyName("total_income")]
        public decimal total_income { get; set; }

        [JsonPropertyName("total_expense")]
        public decimal total_expense { get; set; }
    }
}
