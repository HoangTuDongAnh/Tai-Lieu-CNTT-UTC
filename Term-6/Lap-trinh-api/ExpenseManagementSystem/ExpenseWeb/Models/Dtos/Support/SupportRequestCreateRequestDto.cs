namespace ExpenseWeb.Models.Dtos.Support
{
    public class SupportRequestCreateRequestDto
    {
        public string subject { get; set; } = string.Empty;
        public string message { get; set; } = string.Empty;
        public string support_type { get; set; } = "other";
        public string priority { get; set; } = "medium";
        public List<SupportAttachmentUploadDto> attachments { get; set; } = new();
    }

    public class SupportAttachmentUploadDto
    {
        public string file_name { get; set; } = string.Empty;
        public string file_url { get; set; } = string.Empty;
        public string? file_type { get; set; }
        public long? file_size { get; set; }
    }
}
