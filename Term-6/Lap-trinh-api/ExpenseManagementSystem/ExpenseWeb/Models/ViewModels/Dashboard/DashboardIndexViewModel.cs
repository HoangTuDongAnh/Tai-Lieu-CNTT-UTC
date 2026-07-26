using System.Collections.Generic;

namespace ExpenseWeb.Models.ViewModels.Dashboard
{
    public class DashboardIndexViewModel
    {
        public List<DashboardWalletItemViewModel> Wallets { get; set; } = new();

        public decimal TotalBalance { get; set; }
        public decimal MonthlyIncome { get; set; }
        public decimal MonthlyExpense { get; set; }
        public decimal NetCashflow => MonthlyIncome - MonthlyExpense;
        public int TransactionCount { get; set; }

        public int SelectedMonth { get; set; }
        public int SelectedYear { get; set; }
        public string Granularity { get; set; } = "day";
        public string PeriodPreset { get; set; } = "month";
        public string PeriodLabel { get; set; } = string.Empty;
        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }

        public decimal TodayIncome { get; set; }
        public decimal TodayExpense { get; set; }
        public int TodayIncomeCount { get; set; }
        public int TodayExpenseCount { get; set; }
        public int ActiveWalletCount { get; set; }

        public decimal RangeIncome { get; set; }
        public decimal RangeExpense { get; set; }
        public decimal RangeNetChange { get; set; }
        public int RangeTransactionCount { get; set; }
        public decimal RangeAverageExpense { get; set; }
        public string BusiestLabel { get; set; } = string.Empty;

        public List<DashboardCashflowSeriesItemViewModel> CashflowSeries { get; set; } = new();
        public List<DashboardReportPointViewModel> MonthlyTrend { get; set; } = new();
        public List<DashboardCategoryBreakdownViewModel> CategoryBreakdown { get; set; } = new();
        public List<DashboardCategoryBreakdownViewModel> TodayExpenseBreakdown { get; set; } = new();
        public List<DashboardCategoryBreakdownViewModel> TodayIncomeBreakdown { get; set; } = new();
        public int BudgetAttentionCount { get; set; }
        public int BudgetOverCount { get; set; }
        public int BudgetReachedCount { get; set; }
        public int BudgetWarningCount { get; set; }
        public List<DashboardTopExpenseItemViewModel> TopExpenses { get; set; } = new();
        public List<DashboardRecentTransactionItemViewModel> RecentTransactions { get; set; } = new();
        public List<DashboardBudgetProgressItemViewModel> BudgetProgress { get; set; } = new();

        public string? ErrorMessage { get; set; }
        public string? ReportErrorMessage { get; set; }
    }
}
