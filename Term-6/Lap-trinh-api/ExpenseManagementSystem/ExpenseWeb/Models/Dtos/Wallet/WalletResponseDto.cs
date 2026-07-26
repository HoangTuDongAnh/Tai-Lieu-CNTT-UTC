namespace ExpenseWeb.Models.Dtos.Wallet
{
    public class WalletResponseDto
    {
        public string wallet_id { get; set; } = string.Empty;
        public string user_id { get; set; } = string.Empty;
        public string wallet_name { get; set; } = string.Empty;
        public decimal initial_balance { get; set; }
        public decimal current_balance { get; set; }
        public string currency { get; set; } = "VND";
        public bool is_default { get; set; }
    }
}
