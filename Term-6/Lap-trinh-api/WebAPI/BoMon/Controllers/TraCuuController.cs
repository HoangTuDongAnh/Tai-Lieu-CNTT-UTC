using System.Text.Json;
using BoMon.Models;
using Microsoft.AspNetCore.Mvc;

namespace BoMon.Controllers
{
    public class TraCuuController : Controller
    {
        private readonly IHttpClientFactory _httpClientFactory;

        public TraCuuController(IHttpClientFactory httpClientFactory)
        {
            _httpClientFactory = httpClientFactory;
        }

        [HttpGet]
        public async Task<IActionResult> Index(string? gioiTinh, string? tenBoMon)
        {
            SearchViewModel model = new SearchViewModel
            {
                GioiTinh = gioiTinh,
                TenBoMon = tenBoMon
            };

            try
            {
                var client = _httpClientFactory.CreateClient();

                string apiUrl = "http://127.0.0.1:5000/gv/search"
                    + "?gioiTinh=" + Uri.EscapeDataString(gioiTinh ?? "")
                    + "&tenBoMon=" + Uri.EscapeDataString(tenBoMon ?? "");

                var response = await client.GetAsync(apiUrl);

                if (response.IsSuccessStatusCode)
                {
                    var json = await response.Content.ReadAsStringAsync();

                    var options = new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    };

                    var data = JsonSerializer.Deserialize<List<GiangVienDto>>(json, options);

                    if (data != null)
                    {
                        model.KetQua = data;
                    }
                }
                else
                {
                    ViewBag.Error = "Không gọi được API Python.";
                }
            }
            catch (Exception ex)
            {
                ViewBag.Error = "Lỗi khi gọi API: " + ex.Message;
            }

            return View(model);
        }
    }
}