namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardTopExpenseItemViewModel
    {
        public string TransactionId { get; set; } = string.Empty;
        public DateTime TransactionDate { get; set; }
        public decimal Amount { get; set; }
        public string Note { get; set; } = string.Empty;
        public string WalletName { get; set; } = string.Empty;
        public string CategoryName { get; set; } = string.Empty;
        public string? CategoryIcon { get; set; }
        public string? CategoryColor { get; set; }
    }
}
