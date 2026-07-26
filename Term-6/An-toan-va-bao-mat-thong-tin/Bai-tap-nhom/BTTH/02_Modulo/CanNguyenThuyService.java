public class CanNguyenThuyService
{
    private EuclidService euclidService;
    private HamEulerService hamEulerService;
    private LuyThuaModuloService luyThuaModuloService;

    public CanNguyenThuyService()
    {
        euclidService = new EuclidService();
        hamEulerService = new HamEulerService();
        luyThuaModuloService = new LuyThuaModuloService();
    }

    public boolean laCanNguyenThuy(long a, long n)
    {
        if (n <= 1) {
            return false;
        }

        long aa = ModMath.mod(a, n);

        if (euclidService.gcd(aa, n) != 1) {
            return false;
        }

        long phi = hamEulerService.tinhTheoPhanTich(n);
        long[] uocNguyenTo = layCacUocNguyenToKhacNhau(phi);

        for (int i = 0; i < uocNguyenTo.length; i++) {
            long q = uocNguyenTo[i];
            long soMu = phi / q;
            long giaTri = luyThuaModuloService.tinh(aa, soMu, n);

            if (giaTri == 1) {
                return false;
            }
        }

        return true;
    }

    public long[] layCacUocNguyenToKhacNhau(long n)
    {
        int dem = 0;
        long tam = n;

        for (long p = 2; p * p <= tam; p++) {
            if (tam % p == 0) {
                dem++;
                while (tam % p == 0) {
                    tam = tam / p;
                }
            }
        }

        if (tam > 1) {
            dem++;
        }

        long[] ketQua = new long[dem];
        tam = n;
        int index = 0;

        for (long p = 2; p * p <= tam; p++) {
            if (tam % p == 0) {
                ketQua[index] = p;
                index++;

                while (tam % p == 0) {
                    tam = tam / p;
                }
            }
        }

        if (tam > 1) {
            ketQua[index] = tam;
        }

        return ketQua;
    }
}