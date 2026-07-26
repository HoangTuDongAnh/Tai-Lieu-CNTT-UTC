namespace ExpenseWeb.Models.Dtos.Category
{
    public class CategoryBudgetSummaryDto
    {
        public string? budget_id { get; set; }
        public decimal? limit_amount { get; set; }
        public decimal? spent_amount { get; set; }
        public decimal? remaining_amount { get; set; }
        public double? percentage_used { get; set; }
        public string status { get; set; } = "none";
        public string? period_type { get; set; }
        public int? period_year { get; set; }
        public int? period_month { get; set; }
        public int? period_week { get; set; }
        public DateTime? start_date { get; set; }
        public DateTime? end_date { get; set; }
    }
}