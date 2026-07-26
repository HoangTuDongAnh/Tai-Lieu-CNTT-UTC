namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardReportPointViewModel
    {
        public string Label { get; set; } = string.Empty;
        public decimal Income { get; set; }
        public decimal Expense { get; set; }
        public decimal Balance { get; set; }
        public decimal Net { get; set; }
    }
}
