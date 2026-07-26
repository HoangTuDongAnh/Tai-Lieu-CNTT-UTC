public class EulerLuyThuaService
{
    private EuclidService euclidService;
    private HamEulerService hamEulerService;
    private LuyThuaModuloService luyThuaModuloService;

    public EulerLuyThuaService()
    {
        euclidService = new EuclidService();
        hamEulerService = new HamEulerService();
        luyThuaModuloService = new LuyThuaModuloService();
    }

    public long tinh(long a, long m, long n)
    {
        if (n <= 0) {
            throw new IllegalArgumentException("n phai > 0");
        }

        long aa = ModMath.mod(a, n);

        if (aa == 0) {
            if (m == 0) {
                return 1 % n;
            }
            return 0;
        }

        if (euclidService.gcd(aa, n) != 1) {
            throw new IllegalArgumentException("a va n phai nguyen to cung nhau de ap dung dinh ly Euler");
        }

        long phi = hamEulerService.tinhTheoPhanTich(n);
        long soMuRutGon = m % phi;

        return luyThuaModuloService.tinh(aa, soMuRutGon, n);
    }
}