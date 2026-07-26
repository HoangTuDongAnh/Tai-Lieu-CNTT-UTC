namespace ExpenseWeb.Models.Dtos.Category
{
    public class BudgetResponseDto
    {
        public string budget_id { get; set; } = string.Empty;
        public string user_id { get; set; } = string.Empty;
        public string category_id { get; set; } = string.Empty;
        public string category_type { get; set; } = "expense";
        public decimal limit_amount { get; set; }
        public decimal spent_amount { get; set; }
        public string period_type { get; set; } = string.Empty;
        public int period_year { get; set; }
        public int? period_month { get; set; }
        public int? period_week { get; set; }
        public DateTime? start_date { get; set; }
        public DateTime? end_date { get; set; }
        public decimal remaining_amount { get; set; }
        public double percentage_used { get; set; }
        public string status { get; set; } = "none";
    }
}
