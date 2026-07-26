using System;
using System.ComponentModel.DataAnnotations;

namespace ExpenseWeb.Models.Dtos.Transaction
{
    public class TransactionCreateRequestDto
    {
        [Required] public string wallet_id { get; set; } = string.Empty;
        [Required] public string category_id { get; set; } = string.Empty;
        [Required] public string transaction_type { get; set; } = "expense";
        public decimal amount { get; set; }
        public DateTime transaction_date { get; set; }
        public string? note { get; set; }
        public bool is_recurring { get; set; }
        public string? recur_interval { get; set; }
    }

    public class TransferCreateRequestDto
    {
        [Required] public string from_wallet_id { get; set; } = string.Empty;
        [Required] public string to_wallet_id { get; set; } = string.Empty;
        [Range(0.01, double.MaxValue)] public decimal amount { get; set; }
        public DateTime transfer_date { get; set; }
        public string? note { get; set; }
    }

    public class TransferResponseDto
    {
        public TransactionResponseDto expense { get; set; } = null!;
        public TransactionResponseDto income { get; set; } = null!;
    }
}
