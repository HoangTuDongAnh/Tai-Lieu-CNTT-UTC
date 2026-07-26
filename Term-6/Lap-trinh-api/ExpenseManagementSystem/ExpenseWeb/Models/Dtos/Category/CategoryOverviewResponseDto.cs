namespace ExpenseWeb.Models.Dtos.Category
{
    public class CategoryOverviewResponseDto
    {
        public string category_id { get; set; } = string.Empty;
        public string? user_id { get; set; }
        public string category_name { get; set; } = string.Empty;
        public string? icon { get; set; }
        public string? color { get; set; }
        public bool is_default { get; set; }
        public string? category_type { get; set; }
        public bool can_edit { get; set; }
        public bool can_delete { get; set; }
        public CategoryBudgetSummaryDto budget { get; set; } = new();
        public int transaction_count { get; set; } 
    }
}