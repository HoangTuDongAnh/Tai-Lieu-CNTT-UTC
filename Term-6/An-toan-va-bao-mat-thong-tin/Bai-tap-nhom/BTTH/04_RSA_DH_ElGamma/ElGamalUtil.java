public class ElGamalUtil
{
    public long mod(long a, long n)
    {
        long r = a % n;
        if (r < 0) {
            r = r + n;
        }
        return r;
    }

    public long gcd(long a, long b)
    {
        long A = a;
        long B = b;

        if (A < 0) {
            A = -A;
        }

        if (B < 0) {
            B = -B;
        }

        while (B != 0) {
            long r = A % B;
            A = B;
            B = r;
        }

        return A;
    }

    public long powerMod(long base, long exponent, long mod)
    {
        if (mod <= 0) {
            throw new IllegalArgumentException("mod phai > 0");
        }

        if (exponent < 0) {
            throw new IllegalArgumentException("So mu phai >= 0");
        }

        long result = 1;
        long b = mod(base, mod);
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

    public long modInverse(long a, long m)
    {
        long t = 0;
        long newT = 1;
        long r = m;
        long newR = mod(a, m);

        while (newR != 0) {
            long q = r / newR;

            long tempT = t - q * newT;
            t = newT;
            newT = tempT;

            long tempR = r - q * newR;
            r = newR;
            newR = tempR;
        }

        if (r != 1) {
            throw new IllegalArgumentException("Khong ton tai nghich dao modulo");
        }

        return mod(t, m);
    }
}