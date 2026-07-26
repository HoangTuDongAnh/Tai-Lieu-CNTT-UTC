namespace ExpenseWeb.Models.Dtos.Wallet
{
    public class WalletUpdateRequestDto
    {
        public string? wallet_name { get; set; }
        public string? currency { get; set; }
        public bool? is_default { get; set; }
    }
}
