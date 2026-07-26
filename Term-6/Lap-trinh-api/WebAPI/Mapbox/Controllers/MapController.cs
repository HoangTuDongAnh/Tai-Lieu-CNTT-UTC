using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using Mapbox.Models;

namespace Mapbox.Controllers
{
    public class MapController : Controller
    {
        private readonly MapboxSettings _mapboxSettings;

        public MapController(IOptions<MapboxSettings> mapboxOptions)
        {
            _mapboxSettings = mapboxOptions.Value;
        }

        public IActionResult Index()
        {
            ViewBag.MapboxToken = _mapboxSettings.AccessToken;
            return View();
        }
    }
}