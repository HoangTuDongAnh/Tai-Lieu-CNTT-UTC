public class Bai3_RSA_2
{
    public static void main(String[] args)
    {
        long p = 47;
        long q = 71;
        long e = 61;
        long M = 59;

        RSAService service = new RSAService();
        RSAKeyPair keyPair = service.taoKhoa(p, q, e);

        long C = service.maHoaBaoMat(M, keyPair);
        long MPhay = service.giaiMaBaoMat(C, keyPair);

        System.out.println("===== BAI 3: RSA - BAI TOAN 2 =====");
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

        System.out.println("c) Ba ma hoa thong diep M = " + M + " de gui cho An:");
        System.out.println("C = M^e mod n = " + C);
        System.out.println();

        System.out.println("d) An giai ma ban ma C:");
        System.out.println("M' = C^d mod n = " + MPhay);
        System.out.println();

        System.out.println("e) Ket luan:");
        System.out.println("Viec ma hoa o cau c) thuc hien nhiem vu BAO MAT.");
    }
}