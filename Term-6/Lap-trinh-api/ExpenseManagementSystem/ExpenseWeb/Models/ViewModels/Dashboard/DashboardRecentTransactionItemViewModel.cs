namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardRecentTransactionItemViewModel
    {
        public string TransactionId { get; set; } = string.Empty;
        public System.DateTime TransactionDate { get; set; }
        public string TransactionType { get; set; } = "expense";
        public decimal Amount { get; set; }
        public string WalletName { get; set; } = string.Empty;
        public string CategoryName { get; set; } = string.Empty;
        public string CategoryType { get; set; } = "expense";
        public bool IsTransfer { get; set; }
        public string? CategoryIcon { get; set; }
        public string? CategoryColor { get; set; }
        public string Note { get; set; } = string.Empty;
    }
}
