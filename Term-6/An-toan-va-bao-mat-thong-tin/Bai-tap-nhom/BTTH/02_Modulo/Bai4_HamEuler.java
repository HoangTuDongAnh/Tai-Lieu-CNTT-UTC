public class Bai4_HamEuler
{
    public static void main(String[] args)
    {
        long n = 2863;

        HamEulerService service = new HamEulerService();
        long ketQuaTheoDinhNghia = service.tinhTheoDinhNghia(n);
        long ketQuaTheoPhanTich = service.tinhTheoPhanTich(n);

        System.out.println("===== BAI 4: HAM EULER =====");
        System.out.println("n = " + n);
        System.out.println("Phi(n) theo dinh nghia = " + ketQuaTheoDinhNghia);
        System.out.println("Phi(n) theo phan tich = " + ketQuaTheoPhanTich);
    }
}