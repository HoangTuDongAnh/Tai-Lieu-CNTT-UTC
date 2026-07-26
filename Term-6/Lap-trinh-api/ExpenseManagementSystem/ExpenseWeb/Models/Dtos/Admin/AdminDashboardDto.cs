using System;
using System.Collections.Generic;

namespace ExpenseWeb.Models.Dtos.Admin
{
    public class AdminDashboardDto
    {
        public int total_users { get; set; }
        public int active_users { get; set; }
        public int inactive_users { get; set; }
        public int new_users_today { get; set; }
        public List<int> new_users_week { get; set; } = new();
        public List<string> new_users_week_labels { get; set; } = new();
        public int new_users_week_total { get; set; }
        public int pending_requests { get; set; }
        public int high_priority_open_requests { get; set; }
        public List<AdminDashboardUserDto> recent_users { get; set; } = new();
        public List<AdminDashboardSupportRequestDto> recent_pending_requests { get; set; } = new();
        public int active_today { get; set; }
        public int new_reports { get; set; }
        public DateTime? last_updated_at { get; set; }
    }

    public class AdminDashboardUserDto
    {
        public string user_id { get; set; } = string.Empty;
        public string full_name { get; set; } = string.Empty;
        public string email { get; set; } = string.Empty;
        public string? phone_number { get; set; }
        public string role { get; set; } = string.Empty;
        public string status { get; set; } = string.Empty;
        public string? avatar { get; set; }
        public DateTime created_at { get; set; }
    }

    public class AdminDashboardSupportRequestDto
    {
        public string support_request_id { get; set; } = string.Empty;
        public string subject { get; set; } = string.Empty;
        public string support_type { get; set; } = string.Empty;
        public string priority { get; set; } = string.Empty;
        public string status { get; set; } = string.Empty;
        public DateTime created_at { get; set; }
        public string user_id { get; set; } = string.Empty;
        public string user_full_name { get; set; } = string.Empty;
        public string? user_email { get; set; }
        public string? user_avatar { get; set; }
    }
}
