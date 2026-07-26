public class Bai1_LuyThuaModulo
{
    public static void main(String[] args)
    {
        long a = 499;
        long m = 6337;
        long n = 6337;

        LuyThuaModuloService service = new LuyThuaModuloService();
        long ketQua = service.tinh(a, m, n);

        System.out.println("===== BAI 1: TINH LUY THUA MODULO =====");
        System.out.println("a = " + a);
        System.out.println("m = " + m);
        System.out.println("n = " + n);
        System.out.println("b = a^m mod n = " + ketQua);
    }
}