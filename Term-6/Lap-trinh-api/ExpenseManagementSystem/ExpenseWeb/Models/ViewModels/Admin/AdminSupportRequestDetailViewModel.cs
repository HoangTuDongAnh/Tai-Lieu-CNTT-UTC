using ExpenseWeb.Models.ViewModels.Profile;

namespace ExpenseWeb.Models.ViewModels.Admin
{
    public class AdminSupportRequestDetailViewModel
    {
        public string SupportRequestId { get; set; } = string.Empty;
        public string UserId { get; set; } = string.Empty;
        public string UserFullName { get; set; } = string.Empty;
        public string? UserEmail { get; set; }
        public string? UserAvatar { get; set; }
        public string Subject { get; set; } = string.Empty;
        public string Message { get; set; } = string.Empty;
        public string SupportType { get; set; } = string.Empty;
        public string SupportTypeLabel { get; set; } = string.Empty;
        public string Priority { get; set; } = string.Empty;
        public string PriorityLabel { get; set; } = string.Empty;
        public string Status { get; set; } = string.Empty;
        public string StatusLabel { get; set; } = string.Empty;
        public string? AdminReply { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime? ViewedAt { get; set; }
        public DateTime? RepliedAt { get; set; }
        public DateTime? ClosedAt { get; set; }
        public List<SupportAttachmentViewModel> Attachments { get; set; } = new();
    }
}
