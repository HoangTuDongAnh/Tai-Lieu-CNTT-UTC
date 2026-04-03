import java.util.Scanner;

public class Bai8CanNguyenThuy {

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

    // Kiểm tra a có là căn nguyên thủy của n không
    public static boolean isPrimitiveRoot(long a, long n) {
        a = mod(a, n);

        // Điều kiện cần: gcd(a, n) = 1
        if (gcd(a, n) != 1) {
            return false;
        }

        long phiN = phi(n);

        // Tìm số mũ dương nhỏ nhất sao cho a^m mod n = 1
        for (long m = 1; m < phiN; m++) {
            if (modPow(a, m, n) == 1) {
                return false;
            }
        }

        // Kiểm tra tại phi(n)
        return modPow(a, phiN, n) == 1;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("===== BAI 8: KIEM TRA CAN NGUYEN THUY =====");
        System.out.print("Nhap a = ");
        long a = sc.nextLong();

        System.out.print("Nhap n = ");
        long n = sc.nextLong();

        if (n <= 1) {
            System.out.println("n phai > 1");
            return;
        }

        long phiN = phi(n);
        boolean result = isPrimitiveRoot(a, n);

        System.out.println("\nKet qua:");
        System.out.println("phi(" + n + ") = " + phiN);
        System.out.println("gcd(" + a + ", " + n + ") = " + gcd(a, n));

        if (result) {
            System.out.println(a + " LA can nguyen thuy cua " + n);
        } else {
            System.out.println(a + " KHONG LA can nguyen thuy cua " + n);
        }

        sc.close();
    }
}