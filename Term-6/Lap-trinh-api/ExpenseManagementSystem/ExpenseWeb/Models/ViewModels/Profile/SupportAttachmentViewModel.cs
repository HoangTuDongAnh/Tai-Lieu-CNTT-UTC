namespace ExpenseWeb.Models.ViewModels.Profile
{
    public class SupportAttachmentViewModel
    {
        public int AttachmentId { get; set; }
        public string FileName { get; set; } = string.Empty;
        public string FileUrl { get; set; } = string.Empty;
        public string? FileType { get; set; }
        public long? FileSize { get; set; }
        public string DisplaySize { get; set; } = string.Empty;
        public bool IsImage { get; set; }
        public bool IsVideo { get; set; }
        public bool IsPdf { get; set; }
        public bool IsDocument { get; set; }
    }
}
