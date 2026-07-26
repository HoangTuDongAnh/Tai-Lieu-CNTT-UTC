namespace ExpenseWeb.Models.Dtos.Auth
{
    public class RegisterRequestDto
    {
        public string full_name { get; set; }
        public string email { get; set; }
        public string password { get; set; }
        public string? phone_number { get; set; }
        public string? avatar { get; set; }
        public bool agree_terms { get; set; }
    }
}