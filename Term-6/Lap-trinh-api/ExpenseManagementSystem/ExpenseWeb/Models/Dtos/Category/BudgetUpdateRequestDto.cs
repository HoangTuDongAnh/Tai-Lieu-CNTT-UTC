namespace ExpenseWeb.Models.Dtos.Category
{
    public class BudgetUpdateRequestDto
    {
        public decimal? limit_amount { get; set; }
        public string? period_type { get; set; }
        public int? period_year { get; set; }
        public int? period_month { get; set; }
        public int? period_week { get; set; }
    }
}