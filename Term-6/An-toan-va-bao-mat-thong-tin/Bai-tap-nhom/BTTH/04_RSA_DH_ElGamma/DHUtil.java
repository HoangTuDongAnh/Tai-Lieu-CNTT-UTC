public class DHUtil
{
    public long powerMod(long base, long exponent, long mod)
    {
        if (mod <= 0) {
            throw new IllegalArgumentException("mod phai > 0");
        }

        if (exponent < 0) {
            throw new IllegalArgumentException("So mu phai >= 0");
        }

        long result = 1;
        long b = base % mod;

        if (b < 0) {
            b += mod;
        }

        long e = exponent;

        while (e > 0) {
            if (e % 2 == 1) {
                result = (result * b) % mod;
            }

            b = (b * b) % mod;
            e = e / 2;
        }

        return result;
    }
}