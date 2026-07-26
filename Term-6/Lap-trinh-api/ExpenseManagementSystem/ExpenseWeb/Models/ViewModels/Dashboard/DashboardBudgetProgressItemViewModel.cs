namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardBudgetProgressItemViewModel
    {
        public string BudgetId { get; set; } = string.Empty;
        public string CategoryId { get; set; } = string.Empty;
        public string CategoryName { get; set; } = string.Empty;
        public string? CategoryIcon { get; set; }
        public string? CategoryColor { get; set; }
        public decimal LimitAmount { get; set; }
        public decimal SpentAmount { get; set; }
        public decimal RemainingAmount { get; set; }
        public decimal PercentageUsed { get; set; }
        public string Status { get; set; } = "normal";
    }
}
