namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardCategoryBreakdownViewModel
    {
        public string CategoryId { get; set; } = string.Empty;
        public string CategoryName { get; set; } = string.Empty;
        public string? Icon { get; set; }
        public string Color { get; set; } = "#8592A3";
        public decimal TotalAmount { get; set; }
        public decimal Percentage { get; set; }
    }
}
