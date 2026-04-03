public class ChineseRemainderTheorem {

    // Đưa số về [0, n-1]
    public static long mod(long a, long n) {
        return ((a % n) + n) % n;
    }

    // UCLN
    public static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);

        while (b != 0) {
            long r = a % b;
            a = b;
            b = r;
        }
        return a;
    }

    // Kiểm tra nguyên tố cùng nhau từng đôi
    public static boolean pairwiseCoprime(long... nums) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (gcd(nums[i], nums[j]) != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    // Bình phương và nhân liên tiếp
    public static long modPow(long a, long k, long n) {
        long result = 1;
        a = mod(a, n);

        while (k > 0) {
            if (k % 2 == 1) {
                result = (result * a) % n;
            }
            a = (a * a) % n;
            k /= 2;
        }

        return result;
    }

    // Euclid mở rộng để tìm nghịch đảo modulo
    public static Long modInverse(long a, long n) {
        a = mod(a, n);

        long A1 = 0, A2 = n;
        long B1 = 1, B2 = a;

        while (true) {
            if (B2 == 0) {
                return null;
            }

            if (B2 == 1) {
                return mod(B1, n);
            }

            long Q = A2 / B2;

            long T1 = A1 - Q * B1;
            long T2 = A2 - Q * B2;

            A1 = B1;
            A2 = B2;

            B1 = T1;
            B2 = T2;
        }
    }

    // Giải hệ CRT tổng quát:
    // x ≡ residues[i] (mod moduli[i])
    public static Long solveCRT(long[] residues, long[] moduli) {
        if (residues.length != moduli.length || residues.length == 0) {
            return null;
        }

        if (!pairwiseCoprime(moduli)) {
            return null;
        }

        long M = 1;
        for (long mi : moduli) {
            M *= mi;
        }

        long x = 0;

        for (int i = 0; i < moduli.length; i++) {
            long mi = moduli[i];
            long ai = mod(residues[i], mi);
            long Mi = M / mi;

            Long inv = modInverse(Mi, mi);
            if (inv == null) {
                return null;
            }

            long ci = Mi * inv;
            x = (x + ai * ci) % M;
        }

        return mod(x, M);
    }
}