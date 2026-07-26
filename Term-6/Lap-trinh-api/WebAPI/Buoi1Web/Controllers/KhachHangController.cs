using Microsoft.AspNetCore.Mvc;

namespace Buoi1Web.Controllers
{
    public class KhachHangController : Controller
    {
        public IActionResult Index()
        {
            return View();
        }
    }
}