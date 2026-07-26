public class Bai8_CanNguyenThuy
{
    public static void main(String[] args)
    {
        long a = 11;
        long n = 293;

        CanNguyenThuyService service = new CanNguyenThuyService();
        boolean ketQua = service.laCanNguyenThuy(a, n);

        System.out.println("===== BAI 8: KIEM TRA CAN NGUYEN THUY =====");
        System.out.println("a = " + a);
        System.out.println("n = " + n);

        if (ketQua) {
            System.out.println(a + " la can nguyen thuy cua " + n);
        } else {
            System.out.println(a + " khong phai la can nguyen thuy cua " + n);
        }
    }
}