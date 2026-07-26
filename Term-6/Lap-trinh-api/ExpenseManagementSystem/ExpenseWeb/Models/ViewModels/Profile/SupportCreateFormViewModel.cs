using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Http;

namespace ExpenseWeb.Models.ViewModels.Profile
{
    public class SupportCreateFormViewModel
    {
        [Required(ErrorMessage = "Vui lòng nhập tiêu đề.")]
        [StringLength(200, ErrorMessage = "Tiêu đề tối đa 200 ký tự.")]
        public string Subject { get; set; } = string.Empty;

        [Required(ErrorMessage = "Vui lòng nhập nội dung hỗ trợ.")]
        [StringLength(2000, ErrorMessage = "Nội dung tối đa 2000 ký tự.")]
        public string Message { get; set; } = string.Empty;

        [Required]
        public string SupportType { get; set; } = "bug";

        [Required]
        public string Priority { get; set; } = "medium";

        public List<IFormFile> Files { get; set; } = new();
    }
}
