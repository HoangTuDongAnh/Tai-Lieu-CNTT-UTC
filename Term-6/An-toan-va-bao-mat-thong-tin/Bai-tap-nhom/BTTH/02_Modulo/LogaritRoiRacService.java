public class LogaritRoiRacService
{
    private EuclidService euclidService;
    private HamEulerService hamEulerService;
    private LuyThuaModuloService luyThuaModuloService;

    public LogaritRoiRacService()
    {
        euclidService = new EuclidService();
        hamEulerService = new HamEulerService();
        luyThuaModuloService = new LuyThuaModuloService();
    }

    public Long timLogarit(long a, long b, long n)
    {
        if (n <= 1) {
            return null;
        }

        long aa = ModMath.mod(a, n);
        long bb = ModMath.mod(b, n);

        if (euclidService.gcd(aa, n) != 1) {
            return null;
        }

        long phi = hamEulerService.tinhTheoPhanTich(n);

        for (long k = 0; k < phi; k++) {
            long giaTri = luyThuaModuloService.tinh(aa, k, n);
            if (giaTri == bb) {
                return Long.valueOf(k);
            }
        }

        return null;
    }
}