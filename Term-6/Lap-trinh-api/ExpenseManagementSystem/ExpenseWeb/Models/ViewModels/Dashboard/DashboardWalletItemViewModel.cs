namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardWalletItemViewModel
    {
        public string WalletId { get; set; } = string.Empty;
        public string WalletName { get; set; } = string.Empty;
        public decimal InitialBalance { get; set; }
        public decimal CurrentBalance { get; set; }
        public string Currency { get; set; } = "VND";
        public bool IsDefault { get; set; }
    }
}
