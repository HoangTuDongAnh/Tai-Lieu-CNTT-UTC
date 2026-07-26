namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardDailyCashflowItemViewModel
    {
        public int Day { get; set; }
        public string Label { get; set; } = string.Empty;
        public decimal TotalIncome { get; set; }
        public decimal TotalExpense { get; set; }
    }
}
