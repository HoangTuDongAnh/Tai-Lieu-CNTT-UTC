using System;

namespace ExpenseWeb.Models.ViewModels.Transaction
{
    public class TransactionItemViewModel
    {
        public string TransactionId { get; set; } = string.Empty;
        public string WalletId { get; set; } = string.Empty;
        public string WalletName { get; set; } = string.Empty;
        public string CategoryId { get; set; } = string.Empty;
        public string CategoryName { get; set; } = string.Empty;
        public string CategoryIcon { get; set; } = "bx bx-category";
        public string CategoryColor { get; set; } = "#8592A3";
        public string TransactionType { get; set; } = "expense";
        public bool IsTransfer { get; set; }
        public decimal AmountValue { get; set; }
        public string AmountText { get; set; } = "0 VND";
        public DateTime TransactionDate { get; set; }
        public string TransactionDateText { get; set; } = string.Empty;
        public string Note { get; set; } = string.Empty;
        public string RecurringBadgeText { get; set; } = "Một lần";
        public string Currency { get; set; } = "VND";
    }
}
