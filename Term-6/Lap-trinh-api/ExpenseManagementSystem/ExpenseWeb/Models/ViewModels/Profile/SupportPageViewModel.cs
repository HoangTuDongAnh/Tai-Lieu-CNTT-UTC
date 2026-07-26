namespace ExpenseWeb.Models.ViewModels.Profile
{
    public class SupportPageViewModel
    {
        public string Lang { get; set; } = "vi";
        public string UserFullName { get; set; } = string.Empty;
        public SupportCreateFormViewModel Form { get; set; } = new();
        public List<SupportHistoryItemViewModel> HistoryItems { get; set; } = new();
        public string? SuccessMessage { get; set; }
        public string? ErrorMessage { get; set; }
    }
}
