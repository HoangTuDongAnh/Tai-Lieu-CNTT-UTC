namespace ExpenseWeb.Models.Dtos.Auth
{
    public class UserDto
    {
        public string user_id { get; set; }
        public string full_name { get; set; }
        public string email { get; set; }
        public string phone_number { get; set; }
        public string avatar { get; set; }
        public string role { get; set; }
        public string status { get; set; }
    }
}