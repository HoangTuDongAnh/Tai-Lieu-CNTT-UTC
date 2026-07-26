using System.ComponentModel.DataAnnotations;

namespace ExpenseWeb.Models.ViewModels.Auth
{
    public class ForgotPasswordViewModel
    {
        [Required]
        [EmailAddress]
        public string Email { get; set; }
    }
}