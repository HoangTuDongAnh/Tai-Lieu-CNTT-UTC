namespace ExpenseWeb.Models.Dtos.Category
{
    public class CategoryUpdateRequestDto
    {
        public string? category_name { get; set; }
        public string? icon { get; set; }
        public string? color { get; set; }
        public string? category_type { get; set; }
    }
}