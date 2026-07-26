using System.Text.Json.Serialization;

namespace ExpenseWeb.Models.Dtos.Reports
{
    public class DashboardOverviewDto
    {
        [JsonPropertyName("total_balance")]
        public decimal total_balance { get; set; }

        [JsonPropertyName("monthly_income")]
        public decimal monthly_income { get; set; }

        [JsonPropertyName("monthly_expense")]
        public decimal monthly_expense { get; set; }

        [JsonPropertyName("transaction_count")]
        public int transaction_count { get; set; }
    }
}
