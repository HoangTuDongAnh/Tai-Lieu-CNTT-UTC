public class EuclidService
{
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
            long R = A % B;
            A = B;
            B = R;
        }

        return A;
    }
}