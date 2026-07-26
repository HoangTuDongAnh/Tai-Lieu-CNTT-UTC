namespace ExpenseWeb.Models.Dtos.Support
{
    public class SupportRequestListItemDto
    {
        public string support_request_id { get; set; } = string.Empty;
        public string user_id { get; set; } = string.Empty;
        public string user_full_name { get; set; } = string.Empty;
        public string? user_email { get; set; }
        public string subject { get; set; } = string.Empty;
        public string support_type { get; set; } = string.Empty;
        public string priority { get; set; } = string.Empty;
        public string status { get; set; } = string.Empty;
        public DateTime created_at { get; set; }
        public DateTime? viewed_at { get; set; }
        public DateTime? replied_at { get; set; }
        public DateTime? closed_at { get; set; }
        public bool has_attachments { get; set; }

        public string? message { get; set; }
        public string? admin_reply { get; set; }
    }
}
