import java.util.Scanner;

public class Bai7HePhuongTrinhModuloCRT {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("===== BAI 7: GIAI HE PHUONG TRINH MODULO =====");

        System.out.print("Nhap m1 = ");
        long m1 = sc.nextLong();

        System.out.print("Nhap m2 = ");
        long m2 = sc.nextLong();

        System.out.print("Nhap m3 = ");
        long m3 = sc.nextLong();

        System.out.print("Nhap a1 = ");
        long a1 = sc.nextLong();

        System.out.print("Nhap a2 = ");
        long a2 = sc.nextLong();

        System.out.print("Nhap a3 = ");
        long a3 = sc.nextLong();

        if (!ChineseRemainderTheorem.pairwiseCoprime(m1, m2, m3)) {
            System.out.println("Loi: m1, m2, m3 phai nguyen to cung nhau tung doi mot");
            return;
        }

        long[] residues = {a1, a2, a3};
        long[] moduli = {m1, m2, m3};

        Long x = ChineseRemainderTheorem.solveCRT(residues, moduli);

        if (x == null) {
            System.out.println("Khong giai duoc he bang CRT");
        } else {
            long M = m1 * m2 * m3;

            System.out.println("\nKet qua:");
            System.out.println("x = " + x + " (mod " + M + ")");

            System.out.println("\nKiem tra:");
            System.out.println("x mod " + m1 + " = " + (x % m1));
            System.out.println("x mod " + m2 + " = " + (x % m2));
            System.out.println("x mod " + m3 + " = " + (x % m3));
        }

        sc.close();
    }
}