using System;

namespace ExpenseWeb.Models.Dtos.Transaction
{
    public class TransactionResponseDto
    {
        public string transaction_id { get; set; } = string.Empty;
        public string user_id { get; set; } = string.Empty;
        public string wallet_id { get; set; } = string.Empty;
        public string category_id { get; set; } = string.Empty;
        public string transaction_type { get; set; } = string.Empty;
        public decimal amount { get; set; }
        public DateTime transaction_date { get; set; }
        public string? note { get; set; }
        public bool is_recurring { get; set; }
        public string? recur_interval { get; set; }
    }
}
