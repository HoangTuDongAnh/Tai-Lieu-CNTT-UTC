using System.ComponentModel.DataAnnotations;

namespace ExpenseWeb.Models.ViewModels.Auth
{
    public class RegisterViewModel
    {
        [Required(ErrorMessage = "Full name is required.")]
        public string FullName { get; set; }

        [Required(ErrorMessage = "Email is required.")]
        [EmailAddress(ErrorMessage = "Invalid email format.")]
        public string Email { get; set; }

        [Required(ErrorMessage = "Password is required.")]
        [MinLength(6, ErrorMessage = "Password must be at least 6 characters.")]
        public string Password { get; set; }

        [Required(ErrorMessage = "Confirm password is required.")]
        [Compare("Password", ErrorMessage = "Confirm password does not match.")]
        public string ConfirmPassword { get; set; }

        [Display(Name = "I agree to the terms and conditions")]
        public bool AgreeTerms { get; set; }
    }
}