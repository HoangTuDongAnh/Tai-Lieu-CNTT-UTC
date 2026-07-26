namespace ExpenseWeb.Models.Dtos.Auth
{
    public class RegisterResponseDto
    {
        public UserDto? user { get; set; }
        public bool email_sent { get; set; }
        public string message { get; set; } = string.Empty;
    }
}