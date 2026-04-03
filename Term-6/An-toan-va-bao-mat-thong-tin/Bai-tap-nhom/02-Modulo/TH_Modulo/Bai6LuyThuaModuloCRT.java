import java.util.Scanner;

public class Bai6LuyThuaModuloCRT {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("===== BAI 6: TINH a^k mod n BANG CRT =====");
        System.out.print("Nhap a = ");
        long a = sc.nextLong();

        System.out.print("Nhap k = ");
        long k = sc.nextLong();

        System.out.print("Nhap n = ");
        long n = sc.nextLong();

        if (n <= 1 || k < 0) {
            System.out.println("Du lieu khong hop le");
            return;
        }

        System.out.println("Nhap cach phan tich n thanh 2 modulo nguyen to cung nhau:");
        System.out.print("Nhap m1 = ");
        long m1 = sc.nextLong();

        System.out.print("Nhap m2 = ");
        long m2 = sc.nextLong();

        if (m1 * m2 != n) {
            System.out.println("Loi: m1 * m2 phai bang n");
            return;
        }

        if (!ChineseRemainderTheorem.pairwiseCoprime(m1, m2)) {
            System.out.println("Loi: m1 va m2 phai nguyen to cung nhau");
            return;
        }

        long a1 = ChineseRemainderTheorem.modPow(a, k, m1);
        long a2 = ChineseRemainderTheorem.modPow(a, k, m2);

        long[] residues = {a1, a2};
        long[] moduli = {m1, m2};

        Long result = ChineseRemainderTheorem.solveCRT(residues, moduli);

        if (result == null) {
            System.out.println("Khong giai duoc bang CRT");
        } else {
            System.out.println("\nKet qua trung gian:");
            System.out.println("a^k mod " + m1 + " = " + a1);
            System.out.println("a^k mod " + m2 + " = " + a2);

            System.out.println("\nKet qua cuoi:");
            System.out.println("b = " + a + "^" + k + " mod " + n + " = " + result);
        }

        sc.close();
    }
}