public class Bai9_LogaritRoiRac
{
    public static void main(String[] args)
    {
        long a = 3;
        long b = 8;
        long n = 19;

        LogaritRoiRacService service = new LogaritRoiRacService();
        Long k = service.timLogarit(a, b, n);

        System.out.println("===== BAI 9: LOGARIT ROI RAC =====");
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("n = " + n);

        if (k == null) {
            System.out.println("Khong tim thay k sao cho a^k mod n = b");
        } else {
            System.out.println("k = log_" + a + "(" + b + ") mod " + n + " = " + k);
        }
    }
}