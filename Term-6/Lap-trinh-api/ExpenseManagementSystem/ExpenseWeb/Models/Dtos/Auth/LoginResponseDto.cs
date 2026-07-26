namespace ExpenseWeb.Models.Dtos.Auth
{
    public class LoginResponseDto
    {
        public string access_token { get; set; }
        public string token_type { get; set; }
        public UserDto user { get; set; }
    }
}