using System.ComponentModel.DataAnnotations;

namespace ExpenseWeb.Models.ViewModels.Auth
{
    public class VerifyOtpViewModel
    {
        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;

        [Required(ErrorMessage = "OTP is required.")]
        [StringLength(6, MinimumLength = 4, ErrorMessage = "OTP must be 4 to 6 digits.")]
        public string Otp { get; set; } = string.Empty;
    }
}
