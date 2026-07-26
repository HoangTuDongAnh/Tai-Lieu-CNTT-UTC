public class FermatService
{
    private KiemTraNguyenToService kiemTraNguyenToService;
    private EuclidService euclidService;
    private LuyThuaModuloService luyThuaModuloService;

    public FermatService()
    {
        kiemTraNguyenToService = new KiemTraNguyenToService();
        euclidService = new EuclidService();
        luyThuaModuloService = new LuyThuaModuloService();
    }

    public long tinh(long a, long m, long n)
    {
        if (!kiemTraNguyenToService.laSoNguyenTo(n)) {
            throw new IllegalArgumentException("n phai la so nguyen to de ap dung dinh ly Fermat");
        }

        long aa = ModMath.mod(a, n);

        if (aa == 0) {
            if (m == 0) {
                return 1 % n;
            }
            return 0;
        }

        if (euclidService.gcd(aa, n) != 1) {
            throw new IllegalArgumentException("a va n phai nguyen to cung nhau");
        }

        long soMuRutGon = m % (n - 1);
        return luyThuaModuloService.tinh(aa, soMuRutGon, n);
    }
}