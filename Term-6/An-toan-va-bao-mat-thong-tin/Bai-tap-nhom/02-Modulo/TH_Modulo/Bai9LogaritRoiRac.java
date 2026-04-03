import java.util.Scanner;

public class Bai9LogaritRoiRac {

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

    // Tính phi(n)
    public static long phi(long n) {
        long result = n;
        long temp = n;

        for (long p = 2; p * p <= temp; p++) {
            if (temp % p == 0) {
                while (temp % p == 0) {
                    temp /= p;
                }
                result = result - result / p;
            }
        }

        if (temp > 1) {
            result = result - result / temp;
        }

        return result;
    }

    // Bình phương và nhân liên tiếp
    public static long modPow(long a, long m, long n) {
        long result = 1;
        a = mod(a, n);

        while (m > 0) {
            if (m % 2 == 1) {
                result = (result * a) % n;
            }
            a = (a * a) % n;
            m /= 2;
        }

        return result;
    }

    // Kiểm tra căn nguyên thủy
    public static boolean isPrimitiveRoot(long a, long n) {
        a = mod(a, n);

        if (gcd(a, n) != 1) {
            return false;
        }

        long phiN = phi(n);

        for (long m = 1; m < phiN; m++) {
            if (modPow(a, m, n) == 1) {
                return false;
            }
        }

        return modPow(a, phiN, n) == 1;
    }

    // Tìm k sao cho a^k ≡ b (mod n)
    public static Long discreteLog(long a, long b, long n) {
        a = mod(a, n);
        b = mod(b, n);

        for (long k = 0; k < n; k++) {
            if (modPow(a, k, n) == b) {
                return k;
            }
        }

        return null;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("===== BAI 9: LOGARIT ROI RAC =====");
        System.out.print("Nhap a = ");
        long a = sc.nextLong();

        System.out.print("Nhap b = ");
        long b = sc.nextLong();

        System.out.print("Nhap n = ");
        long n = sc.nextLong();

        if (n <= 1) {
            System.out.println("n phai > 1");
            return;
        }

        Long k = discreteLog(a, b, n);

        System.out.println("\nThong tin:");
        System.out.println("gcd(a, n) = " + gcd(a, n));
        System.out.println("gcd(b, n) = " + gcd(b, n));
        System.out.println("phi(" + n + ") = " + phi(n));
        System.out.println("a co la can nguyen thuy cua n khong? " + (isPrimitiveRoot(a, n) ? "Co" : "Khong"));

        System.out.println("\nKet qua:");
        if (k == null) {
            System.out.println("Khong ton tai k sao cho " + a + "^k ≡ " + b + " (mod " + n + ")");
        } else {
            System.out.println("k = log_" + a + "(" + b + ") mod " + n + " = " + k);
            System.out.println("Kiem tra: " + a + "^" + k + " mod " + n + " = " + modPow(a, k, n));
        }

        sc.close();
    }
}