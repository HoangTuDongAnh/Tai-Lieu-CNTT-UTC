public class BaiTap3_BangTrieuPhuKeCuop {
    public static void main(String[] args) {
        System.out.println("BAI TAP 3: BANG MO TA BAI TOAN TRIEU PHU VA KE CUOP");
        System.out.println("Trang thai co dang: (a,b,k)");
        System.out.println("a: so trieu phu o bo trai");
        System.out.println("b: so ke cuop o bo trai");
        System.out.println("k = 1: thuyen o bo trai, k = 0: thuyen o bo phai");
        System.out.println("Trang thai dau: (3,3,1)");
        System.out.println("Trang thai ket thuc: (0,0,0)");

        TimKiemRong bfs = new TimKiemRong();
        TrangThaiTimKiem ketQuaBFS = bfs.timKiem(
                new TrangThaiTrieuPhuKeCuop(3, 3, 1),
                true
        );
        TienIchTimKiem.inDuongDi(ketQuaBFS);

        TimKiemSau dfs = new TimKiemSau();
        TrangThaiTimKiem ketQuaDFS = dfs.timKiem(
                new TrangThaiTrieuPhuKeCuop(3, 3, 1),
                true
        );
        TienIchTimKiem.inDuongDi(ketQuaDFS);
    }
}