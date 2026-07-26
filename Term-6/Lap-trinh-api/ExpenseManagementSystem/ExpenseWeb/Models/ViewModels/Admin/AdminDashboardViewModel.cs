using System;
using System.Collections.Generic;

namespace ExpenseWeb.Models.ViewModels.Admin
{
    public class AdminDashboardViewModel
    {
        public int TotalUsers { get; set; }
        public int ActiveUsers { get; set; }
        public int InactiveUsers { get; set; }
        public int PendingRequests { get; set; }
        public int NewUsersToday { get; set; }
        public int NewUsersWeekTotal { get; set; }
        public int HighPriorityOpenRequests { get; set; }
        public DateTime? LastUpdatedAt { get; set; }

        public List<int> NewUsersWeek { get; set; } = new();
        public List<string> NewUsersWeekLabels { get; set; } = new();
        public List<AdminUserItemViewModel> RecentUsers { get; set; } = new();
        public List<AdminSupportQueueItemViewModel> RecentPendingRequests { get; set; } = new();
    }

    public class AdminUserItemViewModel
    {
        public string UserId { get; set; } = string.Empty;
        public string FullName { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string? PhoneNumber { get; set; }
        public string Role { get; set; } = string.Empty;
        public string Status { get; set; } = string.Empty;
        public string? Avatar { get; set; }
        public DateTime CreatedAt { get; set; }
    }

    public class AdminSupportQueueItemViewModel
    {
        public string SupportRequestId { get; set; } = string.Empty;
        public string Subject { get; set; } = string.Empty;
        public string SupportType { get; set; } = string.Empty;
        public string Priority { get; set; } = string.Empty;
        public string Status { get; set; } = string.Empty;
        public DateTime CreatedAt { get; set; }
        public string UserId { get; set; } = string.Empty;
        public string UserFullName { get; set; } = string.Empty;
        public string? UserEmail { get; set; }
        public string? UserAvatar { get; set; }
    }
}
