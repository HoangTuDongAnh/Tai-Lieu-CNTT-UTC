import java.util.Scanner;

public class EuclidMoRong {

    // Chuẩn hóa số về [0, n-1]
    public static long mod(long a, long n) {
        return ((a % n) + n) % n;
    }

    // Tìm nghịch đảo của a theo mod n bằng Euclid mở rộng
    public static Long modInverse(long a, long n) {
        a = mod(a, n);

        long A1 = 0, A2 = n;
        long B1 = 1, B2 = a;

        while (true) {
            // Không có nghịch đảo
            if (B2 == 0) {
                return null;
            }

            // Tìm được nghịch đảo
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
        long a = 2705, n = 6577;

        Long x = modInverse(a, n);

        System.out.println("Ket qua:");
        if (x == null) {
            System.out.println("Khong ton tai nghich dao cua " + a + " theo modulo " + n);
        } else {
            System.out.println("x = " + a + "^-1 mod " + n + " = " + x);
            System.out.println("Kiem tra: (" + mod(a, n) + " * " + x + ") mod " + n + " = " + ((mod(a, n) * x) % n));
        }
    }
}