public class ModMath
{
    public static long mod(long a, long n)
    {
        if (n <= 0) {
            throw new IllegalArgumentException("n phai > 0");
        }

        long r = a % n;
        if (r < 0) {
            r = r + n;
        }

        return r;
    }
}