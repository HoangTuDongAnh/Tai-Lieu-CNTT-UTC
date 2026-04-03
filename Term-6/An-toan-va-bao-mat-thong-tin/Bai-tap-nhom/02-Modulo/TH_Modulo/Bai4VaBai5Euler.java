import java.util.Scanner;

public class Bai4VaBai5Euler {

    // Đưa số về [0, n-1]
    public static long mod(long a, long n) {
        return ((a % n) + n) % n;
    }

    // Tính UCLN
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

    // Bài 4: Tính phi(n)
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

    // Bài 5: Dùng định lý Euler để tính a^m mod n
    public static long eulerModulo(long a, long m, long n) {
        a = mod(a, n);

        if (m == 0) return 1 % n;
        if (a == 0) return 0;

        long phiN = phi(n);

        // Nếu a và n nguyên tố cùng nhau thì áp dụng Euler
        if (gcd(a, n) == 1) {
            long reducedExponent = m % phiN;

            // Tránh trường hợp reducedExponent = 0 nhưng m > 0
            if (reducedExponent == 0) {
                reducedExponent = phiN;
            }

            return modPow(a, reducedExponent, n);
        } else {
            // Không thỏa gcd(a,n)=1 thì tính trực tiếp
            return modPow(a, m, n);
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("===== BAI 4 + BAI 5 =====");
        System.out.println("1. Tinh gia tri ham Euler phi(n)");
        System.out.println("2. Dung dinh ly Euler tinh a^m mod n");
        System.out.print("Chon: ");
        int choice = sc.nextInt();

        if (choice == 1) {
            System.out.print("Nhap n = ");
            long n = sc.nextLong();

            if (n <= 0) {
                System.out.println("n phai > 0");
                return;
            }

            long result = phi(n);
            System.out.println("phi(" + n + ") = " + result);

        } else if (choice == 2) {
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

            long phiN = phi(n);
            long result = eulerModulo(a, m, n);

            System.out.println("phi(" + n + ") = " + phiN);
            System.out.println("gcd(a, n) = " + gcd(a, n));

            if (gcd(a, n) == 1) {
                long reducedExponent = m % phiN;
                if (reducedExponent == 0 && m > 0) {
                    reducedExponent = phiN;
                }
                System.out.println("So mu sau khi rut gon theo Euler = " + reducedExponent);
            } else {
                System.out.println("Khong ap dung truc tiep dinh ly Euler vi gcd(a, n) != 1");
            }

            System.out.println("b = a^m mod n = " + result);

        } else {
            System.out.println("Lua chon khong hop le");
        }

        sc.close();
    }
}