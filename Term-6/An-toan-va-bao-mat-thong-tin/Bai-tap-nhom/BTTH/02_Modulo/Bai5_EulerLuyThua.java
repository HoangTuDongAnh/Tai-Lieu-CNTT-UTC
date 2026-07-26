public class Bai5_EulerLuyThua
{
    public static void main(String[] args)
    {
        long a = 27;
        long m = 2201;
        long n = 5400;

        SoDuTrungHoaService service = new SoDuTrungHoaService();
        long[] tach = service.tachMod(n);
        long ketQua = service.tinhLuyThuaTheoTachMod(a, m, n);

        System.out.println("===== BAI 5: EULER THEO HUONG TACH MOD =====");
        System.out.println("a = " + a);
        System.out.println("m = " + m);
        System.out.println("n = " + n);

        System.out.print("Tach n thanh: ");
        inMang(tach);

        System.out.println("Ket qua a^m mod n = " + ketQua);
    }

    private static void inMang(long[] mang)
    {
        for (int i = 0; i < mang.length; i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(mang[i]);
        }
        System.out.println();
    }
}