public class Bai2_NghichDaoModulo
{
    public static void main(String[] args)
    {
        long a = 2705;
        long n = 6577;

        NghichDaoModuloService service = new NghichDaoModuloService();

        Long ketQuaTheoDinhNghia = service.timTheoDinhNghia(a, n);
        Long ketQuaTheoEuclidMoRong = service.timTheoEuclidMoRong(a, n);

        System.out.println("===== BAI 2: TIM NGHICH DAO MODULO =====");
        System.out.println("a = " + a);
        System.out.println("n = " + n);

        if (ketQuaTheoDinhNghia == null) {
            System.out.println("Theo dinh nghia: khong ton tai nghich dao.");
        } else {
            System.out.println("Theo dinh nghia: x = " + ketQuaTheoDinhNghia);
        }

        if (ketQuaTheoEuclidMoRong == null) {
            System.out.println("Theo Euclid mo rong: khong ton tai nghich dao.");
        } else {
            System.out.println("Theo Euclid mo rong: x = " + ketQuaTheoEuclidMoRong);
        }
    }
}