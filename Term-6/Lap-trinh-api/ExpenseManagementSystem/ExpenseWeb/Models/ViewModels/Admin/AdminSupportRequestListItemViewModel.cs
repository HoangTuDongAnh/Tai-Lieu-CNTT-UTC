namespace ExpenseWeb.Models.ViewModels.Admin
{
    public class AdminSupportRequestListItemViewModel
    {
        public string SupportRequestId { get; set; } = string.Empty;
        public string Subject { get; set; } = string.Empty;
        public string UserFullName { get; set; } = string.Empty;
        public string? UserEmail { get; set; }
        public string SupportType { get; set; } = string.Empty;
        public string SupportTypeLabel { get; set; } = string.Empty;
        public string SupportTypeBadgeClass { get; set; } = "bg-label-secondary";
        public string Priority { get; set; } = string.Empty;
        public string PriorityLabel { get; set; } = string.Empty;
        public string PriorityBadgeClass { get; set; } = "bg-label-secondary";
        public string Status { get; set; } = string.Empty;
        public string StatusLabel { get; set; } = string.Empty;
        public string StatusBadgeClass { get; set; } = "bg-label-secondary";
        public DateTime CreatedAt { get; set; }
        public bool HasAttachments { get; set; }
    }
}
