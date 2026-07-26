using System;
using System.ComponentModel.DataAnnotations;

namespace ExpenseWeb.Models.Dtos.Transaction
{
    public class TransactionUpdateRequestDto
    {
        public string? wallet_id { get; set; }
        public string? category_id { get; set; }

        [RegularExpression("^(income|expense)$")]
        public string? transaction_type { get; set; }

        [Range(typeof(decimal), "0.01", "999999999999999")]
        public decimal? amount { get; set; }

        public DateTime? transaction_date { get; set; }

        [MaxLength(500)]
        public string? note { get; set; }

        public bool? is_recurring { get; set; }
        public string? recur_interval { get; set; }
    }
}
