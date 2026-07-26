public class BieuThucModuloService
{
    private LuyThuaModuloService luyThuaModuloService;
    private NghichDaoModuloService nghichDaoModuloService;

    public BieuThucModuloService()
    {
        luyThuaModuloService = new LuyThuaModuloService();
        nghichDaoModuloService = new NghichDaoModuloService();
    }

    public long tinhA1(long a, long b, long x, long y, long n)
    {
        long ax = luyThuaModuloService.tinh(a, x, n);
        long by = luyThuaModuloService.tinh(b, y, n);

        return ModMath.mod(ax + by, n);
    }

    public long tinhA2(long a, long b, long x, long y, long n)
    {
        long ax = luyThuaModuloService.tinh(a, x, n);
        long by = luyThuaModuloService.tinh(b, y, n);

        return ModMath.mod(ax - by, n);
    }

    public long tinhA3(long a, long b, long x, long y, long n)
    {
        long ax = luyThuaModuloService.tinh(a, x, n);
        long by = luyThuaModuloService.tinh(b, y, n);

        return ModMath.mod(ax * by, n);
    }

    public Long tinhA4(long b, long y, long n)
    {
        long by = luyThuaModuloService.tinh(b, y, n);
        return nghichDaoModuloService.timTheoEuclidMoRong(by, n);
    }

    public Long tinhA5(long a, long b, long x, long y, long n)
    {
        long ax = luyThuaModuloService.tinh(a, x, n);
        long by = luyThuaModuloService.tinh(b, y, n);

        Long nghichDao = nghichDaoModuloService.timTheoEuclidMoRong(by, n);
        if (nghichDao == null) {
            return null;
        }

        return Long.valueOf(ModMath.mod(ax * nghichDao.longValue(), n));
    }
}