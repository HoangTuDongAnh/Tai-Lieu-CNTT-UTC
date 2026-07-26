public class PhanTichModuloService
{
    public long[] tachThanhLuyThuaNguyenTo(long n)
    {
        if (n <= 1) {
            throw new IllegalArgumentException("n phai > 1");
        }

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
                long luyThua = 1;
                while (tam % p == 0) {
                    tam = tam / p;
                    luyThua = luyThua * p;
                }
                ketQua[index] = luyThua;
                index++;
            }
        }

        if (tam > 1) {
            ketQua[index] = tam;
        }

        return ketQua;
    }
}