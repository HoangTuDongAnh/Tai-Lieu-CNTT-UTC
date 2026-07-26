namespace ExpenseWeb.Models.Dtos.Category
{
    public class BudgetCreateRequestDto
    {
        public string category_id { get; set; } = string.Empty;
        public decimal limit_amount { get; set; }
        public string period_type { get; set; } = "month";
        public int period_year { get; set; }
        public int? period_month { get; set; }
        public int? period_week { get; set; }
    }
}