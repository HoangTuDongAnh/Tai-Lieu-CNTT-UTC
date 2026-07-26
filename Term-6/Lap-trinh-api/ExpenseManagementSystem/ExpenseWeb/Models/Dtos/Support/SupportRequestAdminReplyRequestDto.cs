namespace ExpenseWeb.Models.Dtos.Support
{
    public class SupportRequestAdminReplyRequestDto
    {
        public string? admin_reply { get; set; }
        public string status { get; set; } = "replied";
    }
}
