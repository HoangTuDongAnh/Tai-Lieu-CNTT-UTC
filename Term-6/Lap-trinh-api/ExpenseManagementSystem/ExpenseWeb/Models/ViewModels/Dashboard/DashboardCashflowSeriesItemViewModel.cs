namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardCashflowSeriesItemViewModel
    {
        public string Key { get; set; } = string.Empty;
        public string Label { get; set; } = string.Empty;
        public decimal Income { get; set; }
        public decimal Expense { get; set; }
        public decimal Net { get; set; }
        public decimal RunningBalance { get; set; }
    }
}
