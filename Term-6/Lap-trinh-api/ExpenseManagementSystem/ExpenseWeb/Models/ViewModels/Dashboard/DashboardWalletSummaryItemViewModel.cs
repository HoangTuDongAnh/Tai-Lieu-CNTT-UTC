namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardWalletSummaryItemViewModel
    {
        public string WalletId { get; set; } = string.Empty;
        public string WalletName { get; set; } = string.Empty;
        public string Currency { get; set; } = "VND";
        public decimal TotalIncome { get; set; }
        public decimal TotalExpense { get; set; }
        public int TransactionCount { get; set; }
    }
}
