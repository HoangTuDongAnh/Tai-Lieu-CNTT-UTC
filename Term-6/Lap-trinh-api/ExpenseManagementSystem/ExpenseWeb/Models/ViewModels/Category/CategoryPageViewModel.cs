using ExpenseWeb.Models.Dtos.Category;

namespace ExpenseWeb.Models.ViewModels.Category
{
    public class CategoryPageViewModel
    {
        public string Lang { get; set; } = "vi";
        public string CurrentPeriodType { get; set; } = "month";
        public int CurrentYear { get; set; }
        public int? CurrentMonth { get; set; }
        public int? CurrentWeek { get; set; }
        public List<CategoryOverviewResponseDto> Categories { get; set; } = new();
    }
}