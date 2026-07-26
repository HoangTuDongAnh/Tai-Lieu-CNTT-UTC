using System.Text.Json.Serialization;

namespace ExpenseWeb.Models.Dtos.Reports
{
    public class DailyCashflowItemDto
    {
        [JsonPropertyName("day")]
        public int day { get; set; }

        [JsonPropertyName("label")]
        public string label { get; set; } = string.Empty;

        [JsonPropertyName("total_income")]
        public decimal total_income { get; set; }

        [JsonPropertyName("total_expense")]
        public decimal total_expense { get; set; }
    }
}
