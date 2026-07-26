public class BaiTap4_Bang8So {
    public static void main(String[] args) {
        String dau = "2831647_5";
        String dich = "1238_4765";

        System.out.println("BAI TAP 4: BANG MO TA BAI TOAN 8 SO");
        System.out.println("Ky hieu _: o trong");

        System.out.println("\nTrang thai dau:");
        TrangThai8So.inBanCo(dau);

        System.out.println("\nTrang thai ket thuc:");
        TrangThai8So.inBanCo(dich);

        TimKiemRong bfs = new TimKiemRong();
        TrangThaiTimKiem ketQuaBFS = bfs.timKiem(
                new TrangThai8So(dau),
                true
        );
        TienIchTimKiem.inDuongDi(ketQuaBFS);

        TimKiemSau dfs = new TimKiemSau();
        TrangThaiTimKiem ketQuaDFS = dfs.timKiem(
                new TrangThai8So(dau),
                true
        );
        TienIchTimKiem.inDuongDi(ketQuaDFS);
    }
}