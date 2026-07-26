public class Bai6_CRT_LuyThua
{
    public static void main(String[] args)
    {
        long a = 101;
        long k = 76;
        long n = 49913;

        SoDuTrungHoaService service = new SoDuTrungHoaService();
        long[] tach = service.tachMod(n);
        long ketQua = service.tinhLuyThuaTheoTachMod(a, k, n);

        System.out.println("===== BAI 6: CRT TINH LUY THUA MODULO =====");
        System.out.println("a = " + a);
        System.out.println("k = " + k);
        System.out.println("n = " + n);

        System.out.print("Tach n thanh: ");
        inMang(tach);

        System.out.println("b = a^k mod n = " + ketQua);
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