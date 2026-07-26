public class NghichDaoModuloService
{
    private EuclidMoRongService euclidMoRongService;
    private EuclidService euclidService;

    public NghichDaoModuloService()
    {
        euclidMoRongService = new EuclidMoRongService();
        euclidService = new EuclidService();
    }

    public Long timTheoDinhNghia(long a, long n)
    {
        if (n <= 1) {
            return null;
        }

        long aa = ModMath.mod(a, n);

        for (long x = 0; x < n; x++) {
            if (ModMath.mod(aa * x, n) == 1) {
                return Long.valueOf(x);
            }
        }

        return null;
    }

    public Long timTheoEuclidMoRong(long a, long n)
    {
        if (n <= 1) {
            return null;
        }

        long aa = ModMath.mod(a, n);

        if (euclidService.gcd(aa, n) != 1) {
            return null;
        }

        KetQuaEuclidMoRong ketQua = euclidMoRongService.solve(aa, n);
        return Long.valueOf(ModMath.mod(ketQua.getX(), n));
    }
}