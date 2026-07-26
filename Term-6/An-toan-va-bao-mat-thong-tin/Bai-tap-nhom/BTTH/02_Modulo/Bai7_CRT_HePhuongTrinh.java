public class Bai7_CRT_HePhuongTrinh
{
    public static void main(String[] args)
    {
        long m1 = 11;
        long m2 = 13;
        long m3 = 17;

        long a1 = 6;
        long a2 = 2;
        long a3 = 4;

        PhuongTrinhModulo[] he = new PhuongTrinhModulo[3];
        he[0] = new PhuongTrinhModulo(a1, m1);
        he[1] = new PhuongTrinhModulo(a2, m2);
        he[2] = new PhuongTrinhModulo(a3, m3);

        SoDuTrungHoaService service = new SoDuTrungHoaService();
        long x = service.giaiHe(he);

        System.out.println("===== BAI 7: CRT GIAI HE PHUONG TRINH =====");
        System.out.println("x mod " + m1 + " = " + a1);
        System.out.println("x mod " + m2 + " = " + a2);
        System.out.println("x mod " + m3 + " = " + a3);
        System.out.println("Nghiem x = " + x);
        System.out.println("Nghiem tong quat: x = " + x + " + k*" + (m1 * m2 * m3));
    }
}