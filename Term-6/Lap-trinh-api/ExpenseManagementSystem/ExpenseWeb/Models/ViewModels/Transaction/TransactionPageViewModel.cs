using System.Collections.Generic;
using ExpenseWeb.Models.Dtos.Category;
using ExpenseWeb.Models.Dtos.Wallet;

namespace ExpenseWeb.Models.ViewModels.Transaction
{
    public class TransactionPageViewModel
    {
        public List<TransactionItemViewModel> Transactions { get; set; } = new();
        public List<CategoryResponseDto> Categories { get; set; } = new();
        public List<CategoryResponseDto> ExpenseCategories { get; set; } = new();
        public List<CategoryResponseDto> IncomeCategories { get; set; } = new();
        public List<WalletResponseDto> Wallets { get; set; } = new();
        public string? ErrorMessage { get; set; }
    }
}
