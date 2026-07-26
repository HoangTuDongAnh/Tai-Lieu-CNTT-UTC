public class Bai1_DiffieHellman
{
    public static void main(String[] args)
    {
        DHParams input = new DHParams(7523, 5, 387, 247);
        DHService service = new DHService();

        long q = input.getQ();
        long a = input.getA();
        long xA = input.getXA();
        long xB = input.getXB();

        long yA = service.tinhKhoaCongKhai(a, xA, q);
        long yB = service.tinhKhoaCongKhai(a, xB, q);

        long kA = service.tinhKhoaPhien(yB, xA, q);
        long kB = service.tinhKhoaPhien(yA, xB, q);

        System.out.println("===== BAI 1: TRAO DOI KHOA DIFFIE-HELLMAN =====");
        System.out.println("q = " + q);
        System.out.println("a = " + a);
        System.out.println("xA = " + xA);
        System.out.println("xB = " + xB);
        System.out.println();

        System.out.println("a) Phia An:");
        System.out.println("yA = a^xA mod q = " + yA);
        System.out.println("K  = (yB)^xA mod q = " + kA);
        System.out.println();

        System.out.println("b) Phia Ba:");
        System.out.println("yB = a^xB mod q = " + yB);
        System.out.println("K  = (yA)^xB mod q = " + kB);
        System.out.println();

        if (kA == kB) {
            System.out.println("Hai ben tao cung khoa phien K = " + kA);
        } else {
            System.out.println("Co loi: hai ben khong tao cung mot khoa.");
        }
    }
}