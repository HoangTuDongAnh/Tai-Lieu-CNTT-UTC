using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace ExpenseWeb.Filters
{
    public class RequireRoleAttribute : ActionFilterAttribute
    {
        private readonly string _role;

        public RequireRoleAttribute(string role)
        {
            _role = role.Trim().ToLower();
        }

        public override void OnActionExecuting(ActionExecutingContext context)
        {
            var token = context.HttpContext.Session.GetString("AccessToken");
            var role = context.HttpContext.Session.GetString("UserRole")?.Trim().ToLower();

            if (string.IsNullOrWhiteSpace(token))
            {
                context.Result = new RedirectToActionResult("Login", "Auth", null);
                return;
            }

            if (!string.Equals(role, _role, StringComparison.OrdinalIgnoreCase))
            {
                context.Result = string.Equals(role, "admin", StringComparison.OrdinalIgnoreCase)
                    ? new RedirectToActionResult("Index", "Admin", null)
                    : new RedirectToActionResult("Index", "Dashboard", null);
                return;
            }

            base.OnActionExecuting(context);
        }
    }
}
