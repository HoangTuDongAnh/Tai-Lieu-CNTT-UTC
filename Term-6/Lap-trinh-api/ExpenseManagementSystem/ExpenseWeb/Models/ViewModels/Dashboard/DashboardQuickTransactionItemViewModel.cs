namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardQuickTransactionItemViewModel
    {
        public string TransactionId { get; set; } = string.Empty;
        public string TransactionType { get; set; } = "expense";
        public decimal Amount { get; set; }
        public DateTime TransactionDate { get; set; }
        public string Note { get; set; } = string.Empty;
        public string WalletName { get; set; } = string.Empty;
        public string CategoryName { get; set; } = string.Empty;
        public string? CategoryIcon { get; set; }
        public string? CategoryColor { get; set; }
    }
}
