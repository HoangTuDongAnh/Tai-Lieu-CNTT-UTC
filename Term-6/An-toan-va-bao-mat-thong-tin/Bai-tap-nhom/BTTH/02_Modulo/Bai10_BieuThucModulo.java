public class Bai10_BieuThucModulo
{
    public static void main(String[] args)
    {
        long a = 83;
        long b = 17;
        long x = 354;
        long y = 314;
        long n = 241;

        BieuThucModuloService service = new BieuThucModuloService();

        long A1 = service.tinhA1(a, b, x, y, n);
        long A2 = service.tinhA2(a, b, x, y, n);
        long A3 = service.tinhA3(a, b, x, y, n);
        Long A4 = service.tinhA4(b, y, n);
        Long A5 = service.tinhA5(a, b, x, y, n);

        System.out.println("===== BAI 10: CAC BIEU THUC MODULO CO BAN =====");
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("x = " + x);
        System.out.println("y = " + y);
        System.out.println("n = " + n);
        System.out.println();

        System.out.println("A1 = (a^x + b^y) mod n = " + A1);
        System.out.println("A2 = (a^x - b^y) mod n = " + A2);
        System.out.println("A3 = (a^x * b^y) mod n = " + A3);

        if (A4 == null) {
            System.out.println("A4 = (b^y)^-1 mod n = khong ton tai");
        } else {
            System.out.println("A4 = (b^y)^-1 mod n = " + A4);
        }

        if (A5 == null) {
            System.out.println("A5 = (a^x / b^y) mod n = khong xac dinh");
        } else {
            System.out.println("A5 = (a^x / b^y) mod n = " + A5);
        }
    }
}