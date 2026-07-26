namespace ExpenseWeb.Models.Dtos.Category
{
    public class CategoryCreateRequestDto
    {
        public string category_name { get; set; } = string.Empty;
        public string? icon { get; set; }
        public string? color { get; set; }
        public string? category_type { get; set; }
    }
}