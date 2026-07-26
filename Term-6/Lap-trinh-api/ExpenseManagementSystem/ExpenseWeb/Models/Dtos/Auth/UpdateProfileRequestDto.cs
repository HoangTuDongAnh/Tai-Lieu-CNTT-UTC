namespace ExpenseWeb.Models.Dtos.Auth
{
    public class UpdateProfileRequestDto
    {
        public string full_name { get; set; } = string.Empty;
        public string email { get; set; } = string.Empty;
        public string? phone_number { get; set; }
        public string? avatar { get; set; }
    }
}
