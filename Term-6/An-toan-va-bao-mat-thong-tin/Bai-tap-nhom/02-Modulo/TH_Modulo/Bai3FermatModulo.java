import java.util.Scanner;

public class Bai3FermatModulo {

    // Đưa số về miền [0, n-1]
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

    // Kiểm tra số nguyên tố đơn giản
    public static boolean isPrime(long n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;

        for (long i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
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
            m = m / 2;
        }

        return result;
    }

    // Tính a^m mod n bằng định lý Fermat
    public static long fermatModulo(long a, long m, long n) {
        a = mod(a, n);

        // a ≡ 0 (mod n)
        if (a == 0) {
            if (m == 0) return 1 % n; // quy ước 0^0 nếu cần
            return 0;
        }

        // Giảm số mũ theo Fermat: a^(n-1) ≡ 1 (mod n)
        long reducedExponent = m % (n - 1);

        return modPow(a, reducedExponent, n);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Nhap a = ");
        long a = sc.nextLong();

        System.out.print("Nhap m = ");
        long m = sc.nextLong();

        System.out.print("Nhap n = ");
        long n = sc.nextLong();

        if (n <= 1) {
            System.out.println("n phai > 1");
            return;
        }

        if (m < 0) {
            System.out.println("m phai >= 0");
            return;
        }

        if (!isPrime(n)) {
            System.out.println("Khong ap dung duoc dinh ly Fermat vi n khong phai so nguyen to.");
            return;
        }

        if (mod(a, n) != 0 && gcd(a, n) != 1) {
            System.out.println("Khong thoa dieu kien gcd(a, n) = 1 de ap dung Fermat.");
            return;
        }

        long b = fermatModulo(a, m, n);

        System.out.println("\nKet qua:");
        System.out.println("b = a^m mod n = " + b);

        if (mod(a, n) != 0) {
            System.out.println("So mu sau khi rut gon theo Fermat: m mod (n - 1) = " + (m % (n - 1)));
        }

        sc.close();
    }
}