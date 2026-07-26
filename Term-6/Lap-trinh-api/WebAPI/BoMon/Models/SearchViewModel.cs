namespace BoMon.Models
{
    public class SearchViewModel
    {
        public string? GioiTinh { get; set; }
        public string? TenBoMon { get; set; }
        public List<GiangVienDto> KetQua { get; set; } = new List<GiangVienDto>();
    }
}