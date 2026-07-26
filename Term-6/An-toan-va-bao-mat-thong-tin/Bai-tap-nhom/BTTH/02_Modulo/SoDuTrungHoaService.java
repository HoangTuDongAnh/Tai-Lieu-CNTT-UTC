public class SoDuTrungHoaService
{
    private EuclidService euclidService;
    private NghichDaoModuloService nghichDaoModuloService;
    private LuyThuaModuloService luyThuaModuloService;
    private PhanTichModuloService phanTichModuloService;
    private KiemTraNguyenToService kiemTraNguyenToService;
    private FermatService fermatService;
    private EulerLuyThuaService eulerLuyThuaService;

    public SoDuTrungHoaService()
    {
        euclidService = new EuclidService();
        nghichDaoModuloService = new NghichDaoModuloService();
        luyThuaModuloService = new LuyThuaModuloService();
        phanTichModuloService = new PhanTichModuloService();
        kiemTraNguyenToService = new KiemTraNguyenToService();
        fermatService = new FermatService();
        eulerLuyThuaService = new EulerLuyThuaService();
    }

    public boolean kiemTraNguyenToCungNhauTungDoi(PhuongTrinhModulo[] he)
    {
        for (int i = 0; i < he.length; i++) {
            for (int j = i + 1; j < he.length; j++) {
                if (euclidService.gcd(he[i].getModulo(), he[j].getModulo()) != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public long giaiHe(PhuongTrinhModulo[] he)
    {
        if (he == null || he.length == 0) {
            throw new IllegalArgumentException("He phuong trinh khong hop le");
        }

        if (!kiemTraNguyenToCungNhauTungDoi(he)) {
            throw new IllegalArgumentException("Cac modulo phai nguyen to cung nhau tung doi");
        }

        long M = 1;
        for (int i = 0; i < he.length; i++) {
            M = M * he[i].getModulo();
        }

        long tong = 0;

        for (int i = 0; i < he.length; i++) {
            long mi = he[i].getModulo();
            long ai = ModMath.mod(he[i].getSoDu(), mi);

            long Mi = M / mi;

            Long nghichDao = nghichDaoModuloService.timTheoEuclidMoRong(Mi, mi);
            if (nghichDao == null) {
                throw new IllegalArgumentException("Khong tim duoc nghich dao trong qua trinh ghep CRT");
            }

            long ci = Mi * nghichDao.longValue();
            tong = tong + ai * ci;
        }

        return ModMath.mod(tong, M);
    }

    public long tinhModuloLonBangCRT(long a, long k, long n, long[] cacModulo)
    {
        if (cacModulo == null || cacModulo.length == 0) {
            throw new IllegalArgumentException("Danh sach modulo khong hop le");
        }

        PhuongTrinhModulo[] he = new PhuongTrinhModulo[cacModulo.length];

        long tich = 1;
        for (int i = 0; i < cacModulo.length; i++) {
            tich = tich * cacModulo[i];
        }

        if (tich != n) {
            throw new IllegalArgumentException("Tich cac modulo con phai bang n");
        }

        for (int i = 0; i < cacModulo.length; i++) {
            long mi = cacModulo[i];
            long ai = luyThuaModuloService.tinh(a, k, mi);
            he[i] = new PhuongTrinhModulo(ai, mi);
        }

        return giaiHe(he);
    }

    public long tinhModuloLonBangCRTTuDong(long a, long k, long n)
    {
        long[] cacModulo = phanTichModuloService.tachThanhLuyThuaNguyenTo(n);
        return tinhModuloLonBangCRT(a, k, n, cacModulo);
    }

    public long tinhLuyThuaTheoTachMod(long a, long k, long n)
    {
        long[] cacModulo = phanTichModuloService.tachThanhLuyThuaNguyenTo(n);
        PhuongTrinhModulo[] he = new PhuongTrinhModulo[cacModulo.length];

        for (int i = 0; i < cacModulo.length; i++) {
            long mi = cacModulo[i];
            long ai = tinhTrenModuloCon(a, k, mi);
            he[i] = new PhuongTrinhModulo(ai, mi);
        }

        return giaiHe(he);
    }

    private long tinhTrenModuloCon(long a, long k, long mi)
    {
        long aa = ModMath.mod(a, mi);

        if (aa == 0) {
            if (k == 0) {
                return 1 % mi;
            }
            return 0;
        }

        if (kiemTraNguyenToService.laSoNguyenTo(mi) && euclidService.gcd(aa, mi) == 1) {
            return fermatService.tinh(aa, k, mi);
        }

        if (euclidService.gcd(aa, mi) == 1) {
            return eulerLuyThuaService.tinh(aa, k, mi);
        }

        return luyThuaModuloService.tinh(aa, k, mi);
    }

    public long[] tachMod(long n)
    {
        return phanTichModuloService.tachThanhLuyThuaNguyenTo(n);
    }
}