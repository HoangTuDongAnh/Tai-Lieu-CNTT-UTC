namespace ExpenseWeb.Models.ViewModels.Profile
{
    public class SupportHistoryItemViewModel
    {
        public string SupportRequestId { get; set; } = string.Empty;
        public string Subject { get; set; } = string.Empty;
        public string Message { get; set; } = string.Empty;
        public string SupportType { get; set; } = string.Empty;
        public string SupportTypeLabel { get; set; } = string.Empty;
        public string Priority { get; set; } = string.Empty;
        public string PriorityLabel { get; set; } = string.Empty;
        public string Status { get; set; } = string.Empty;
        public string StatusLabel { get; set; } = string.Empty;
        public string StatusBadgeClass { get; set; } = "bg-label-secondary";
        public DateTime CreatedAt { get; set; }
        public DateTime? RepliedAt { get; set; }
        public string? AdminReply { get; set; }
        public List<SupportAttachmentViewModel> Attachments { get; set; } = new();
    }
}
