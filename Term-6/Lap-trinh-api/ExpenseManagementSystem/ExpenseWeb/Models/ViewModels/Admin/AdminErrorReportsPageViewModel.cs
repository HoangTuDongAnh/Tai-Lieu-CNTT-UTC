namespace ExpenseWeb.Models.ViewModels.Admin
{
    public class AdminErrorReportsPageViewModel
    {
        public string Lang { get; set; } = "vi";
        public string? CurrentStatus { get; set; }
        public string? CurrentSupportType { get; set; }
        public string? CurrentPriority { get; set; }
        public string? Keyword { get; set; }
        public List<AdminSupportRequestListItemViewModel> Items { get; set; } = new();
        public AdminSupportRequestDetailViewModel? SelectedItem { get; set; }
    }
}
