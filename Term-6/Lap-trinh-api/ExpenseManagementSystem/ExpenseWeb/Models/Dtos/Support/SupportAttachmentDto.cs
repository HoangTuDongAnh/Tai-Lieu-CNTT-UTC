namespace ExpenseWeb.Models.Dtos.Support
{
    public class SupportAttachmentDto
    {
        public int attachment_id { get; set; }
        public string support_request_id { get; set; } = string.Empty;
        public string file_name { get; set; } = string.Empty;
        public string file_url { get; set; } = string.Empty;
        public string? file_type { get; set; }
        public long? file_size { get; set; }
        public DateTime created_at { get; set; }
    }
}
