public class HamEulerService
{
    private EuclidService euclidService;

    public HamEulerService()
    {
        euclidService = new EuclidService();
    }

    public long tinhTheoDinhNghia(long n)
    {
        if (n <= 0) {
            throw new IllegalArgumentException("n phai > 0");
        }

        long dem = 0;

        for (long i = 1; i < n; i++) {
            if (euclidService.gcd(i, n) == 1) {
                dem++;
            }
        }

        return dem;
    }

    public long tinhTheoPhanTich(long n)
    {
        if (n <= 0) {
            throw new IllegalArgumentException("n phai > 0");
        }

        long ketQua = n;
        long tam = n;

        for (long p = 2; p * p <= tam; p++) {
            if (tam % p == 0) {
                while (tam % p == 0) {
                    tam = tam / p;
                }
                ketQua = ketQua - ketQua / p;
            }
        }

        if (tam > 1) {
            ketQua = ketQua - ketQua / tam;
        }

        return ketQua;
    }
}