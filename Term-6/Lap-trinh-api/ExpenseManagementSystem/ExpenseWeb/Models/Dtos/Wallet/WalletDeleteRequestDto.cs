namespace ExpenseWeb.Models.Dtos.Wallet
{
    public class WalletDeleteRequestDto
    {
        public string mode { get; set; } = "delete_all";
        public string? replacement_wallet_id { get; set; }
    }
}
