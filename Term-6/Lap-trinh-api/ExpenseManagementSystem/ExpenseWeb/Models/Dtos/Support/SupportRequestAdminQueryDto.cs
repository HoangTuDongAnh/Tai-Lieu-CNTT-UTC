namespace ExpenseWeb.Models.Dtos.Support
{
    public class SupportRequestAdminQueryDto
    {
        public string? status { get; set; }
        public string? support_type { get; set; }
        public string? priority { get; set; }
        public string? keyword { get; set; }
        public int page { get; set; } = 1;
        public int page_size { get; set; } = 20;
    }
}
