namespace ExpenseWeb.Models.Dtos.Wallet
{
    public class WalletCreateRequestDto
    {
        public string wallet_name { get; set; } = string.Empty;
        public decimal initial_balance { get; set; }
        public string currency { get; set; } = "VND";
        public bool is_default { get; set; }
    }
}
