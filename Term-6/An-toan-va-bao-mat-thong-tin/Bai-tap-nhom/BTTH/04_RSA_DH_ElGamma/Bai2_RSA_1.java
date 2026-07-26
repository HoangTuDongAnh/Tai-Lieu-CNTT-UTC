public class Bai2_RSA_1
{
    public static void main(String[] args)
    {
        long p = 47;
        long q = 71;
        long e = 61;
        long M = 59;

        RSAService service = new RSAService();
        RSAKeyPair keyPair = service.taoKhoa(p, q, e);

        long C = service.kySo(M, keyPair);
        long MPhay = service.xacMinhChuKy(C, keyPair);

        System.out.println("===== BAI 2: RSA - BAI TOAN 1 =====");
        System.out.println("Input: p = " + p + ", q = " + q + ", e = " + e);
        System.out.println();

        System.out.println("a) Khoa cong khai cua An:");
        System.out.println("PU = {e, n} = " + keyPair.getPublicKeyString());
        System.out.println();

        System.out.println("b) Khoa rieng cua An:");
        System.out.println("n = p * q = " + keyPair.getN());
        System.out.println("phi(n) = (p - 1)(q - 1) = " + keyPair.getPhi());
        System.out.println("d = e^-1 mod phi(n) = " + keyPair.getD());
        System.out.println("PR = {d, n} = " + keyPair.getPrivateKeyString());
        System.out.println();

        System.out.println("c) An ma hoa thong diep M = " + M);
        System.out.println("C = M^d mod n = " + C);
        System.out.println();

        System.out.println("d) Nguoi nhan kiem tra/giai ma:");
        System.out.println("M' = C^e mod n = " + MPhay);
        System.out.println();

        System.out.println("e) Ket luan:");
        System.out.println("Viec ma hoa o cau c) thuc hien nhiem vu XAC THUC / CHU KY SO.");
    }
}