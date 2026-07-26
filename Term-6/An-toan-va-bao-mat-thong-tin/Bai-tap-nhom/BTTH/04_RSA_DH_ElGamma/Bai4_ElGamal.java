public class Bai4_ElGamal
{
    public static void main(String[] args)
    {
        ElGamalParams input = new ElGamalParams(7433, 3, 341, 872, 403);
        ElGamalService service = new ElGamalService();

        long q = input.getQ();
        long a = input.getA();
        long xA = input.getXA();
        long k = input.getK();
        long M = input.getM();

        long yA = service.tinhKhoaCongKhai(q, a, xA);

        long KMaHoa = service.tinhKMaHoa(q, yA, k);
        ElGamalCipher cipher = service.maHoa(q, a, yA, k, M);

        long KGiaiMa = service.tinhKGiaiMa(q, cipher.getC1(), xA);
        long MPhay = service.giaiMa(q, xA, cipher);

        System.out.println("===== BAI 4: MAT MA ELGAMAL =====");
        System.out.println("q = " + q);
        System.out.println("a = " + a);
        System.out.println("xA = " + xA);
        System.out.println("k = " + k);
        System.out.println("M = " + M);
        System.out.println();

        System.out.println("a) Khoa cong khai cua An:");
        System.out.println("yA = a^xA mod q = " + yA);
        System.out.println("PU = {q, a, yA} = {" + q + ", " + a + ", " + yA + "}");
        System.out.println();

        System.out.println("b) Ba ma hoa thong diep M:");
        System.out.println("K = (yA)^k mod q = " + KMaHoa);
        System.out.println("C1 = a^k mod q = " + cipher.getC1());
        System.out.println("C2 = K * M mod q = " + cipher.getC2());
        System.out.println("Ban ma (C1, C2) = " + cipher.toString());
        System.out.println();

        System.out.println("c) An giai ma ban ma:");
        System.out.println("K = (C1)^xA mod q = " + KGiaiMa);
        System.out.println("M' = C2 * K^-1 mod q = " + MPhay);
    }
}