namespace ExpenseWeb.Models.ViewModels.Profile
{
    public class ProfilePageViewModel
    {
        public string LastName { get; set; } = string.Empty;
        public string FirstName { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string? PhoneNumber { get; set; }
        public string? Avatar { get; set; }
        public string FullName => string.Join(" ", new[] { LastName?.Trim(), FirstName?.Trim() }.Where(x => !string.IsNullOrWhiteSpace(x)));
    }
}
