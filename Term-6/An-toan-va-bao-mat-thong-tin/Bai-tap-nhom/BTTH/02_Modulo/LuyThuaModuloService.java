public class LuyThuaModuloService
{
    public long tinh(long a, long m, long n)
    {
        if (n <= 0) {
            throw new IllegalArgumentException("n phai > 0");
        }

        if (m < 0) {
            throw new IllegalArgumentException("m phai >= 0");
        }

        long ketQua = 1;
        long coSo = ModMath.mod(a, n);
        long soMu = m;

        while (soMu > 0) {
            if (soMu % 2 == 1) {
                ketQua = ModMath.mod(ketQua * coSo, n);
            }

            coSo = ModMath.mod(coSo * coSo, n);
            soMu = soMu / 2;
        }

        return ketQua;
    }
}