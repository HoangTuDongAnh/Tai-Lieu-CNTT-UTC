import java.util.Scanner;

public class Bai10BieuThucModulo {

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

    // Euclid mở rộng tìm nghịch đảo modulo
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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("===== BAI 10: CAC BIEU THUC MODULO CO BAN =====");

        System.out.print("Nhap a = ");
        long a = sc.nextLong();

        System.out.print("Nhap b = ");
        long b = sc.nextLong();

        System.out.print("Nhap x = ");
        long x = sc.nextLong();

        System.out.print("Nhap y = ");
        long y = sc.nextLong();

        System.out.print("Nhap n = ");
        long n = sc.nextLong();

        if (n <= 1) {
            System.out.println("n phai > 1");
            return;
        }

        if (x < 0 || y < 0) {
            System.out.println("x, y phai >= 0");
            return;
        }

        long ax = modPow(a, x, n); // a^x mod n
        long by = modPow(b, y, n); // b^y mod n

        long A1 = mod(ax + by, n);
        long A2 = mod(ax - by, n);
        long A3 = mod(ax * by, n);

        Long A4 = modInverse(by, n);
        Long A5 = null;

        if (A4 != null) {
            A5 = mod(ax * A4, n);
        }

        System.out.println("\nGia tri trung gian:");
        System.out.println("a^x mod n = " + ax);
        System.out.println("b^y mod n = " + by);

        System.out.println("\nKet qua:");
        System.out.println("A1 = (a^x + b^y) mod n = " + A1);
        System.out.println("A2 = (a^x - b^y) mod n = " + A2);
        System.out.println("A3 = (a^x * b^y) mod n = " + A3);

        if (A4 == null) {
            System.out.println("A4 = (b^y)^-1 mod n = Khong ton tai");
            System.out.println("A5 = (a^x / b^y) mod n = Khong ton tai");
            System.out.println("Ly do: gcd(b^y mod n, n) = " + gcd(by, n) + " != 1");
        } else {
            System.out.println("A4 = (b^y)^-1 mod n = " + A4);
            System.out.println("A5 = (a^x / b^y) mod n = " + A5);
        }

        sc.close();
    }
}