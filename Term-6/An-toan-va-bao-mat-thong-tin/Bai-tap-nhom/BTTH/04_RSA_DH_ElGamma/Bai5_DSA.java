public class Bai5_DSA
{
    public static void main(String[] args)
    {
        DSAParams input = new DSAParams(7, 47, 23, 34, 2, 10);
        DSAService service = new DSAService();

        long hM = input.getHM();
        long p = input.getP();
        long q = input.getQ();
        long h = input.getH();
        long xA = input.getXA();
        long k = input.getK();

        long g = service.tinhG(p, q, h);
        long yA = service.tinhKhoaCongKhai(p, g, xA);

        DSASignature signature = service.kySo(hM, p, q, g, xA, k);

        long w = service.tinhW(signature.getS(), q);
        long u1 = service.tinhU1(hM, w, q);
        long u2 = service.tinhU2(signature.getR(), w, q);
        long v = service.tinhV(p, q, g, yA, u1, u2);

        boolean hopLe = service.xacMinh(hM, p, q, g, yA, signature);

        System.out.println("===== BAI 5: CHU KY DIEN TU DSA =====");
        System.out.println("H(M) = " + hM);
        System.out.println("p = " + p);
        System.out.println("q = " + q);
        System.out.println("h = " + h);
        System.out.println("xA = " + xA);
        System.out.println("k = " + k);
        System.out.println();

        System.out.println("Tinh g = h^((p-1)/q) mod p = " + g);
        System.out.println();

        System.out.println("a) Khoa cong khai cua An:");
        System.out.println("yA = g^xA mod p = " + yA);
        System.out.println();

        System.out.println("b) Chu ky so cua An:");
        System.out.println("r = (g^k mod p) mod q = " + signature.getR());
        System.out.println("s = k^-1(H(M) + xA*r) mod q = " + signature.getS());
        System.out.println("Chu ky (r, s) = " + signature.toString());
        System.out.println();

        System.out.println("c) Ba xac minh chu ky:");
        System.out.println("w  = s^-1 mod q = " + w);
        System.out.println("u1 = H(M)*w mod q = " + u1);
        System.out.println("u2 = r*w mod q = " + u2);
        System.out.println("v  = ((g^u1 * yA^u2) mod p) mod q = " + v);
        System.out.println();

        if (hopLe) {
            System.out.println("Ket luan: v = r, chu ky HOP LE.");
        } else {
            System.out.println("Ket luan: v != r, chu ky KHONG hop le.");
        }
    }
}