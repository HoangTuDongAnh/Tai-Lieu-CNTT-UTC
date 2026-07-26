public class BaiTap1_KhongChuTrinh {
    public static void main(String[] args) {
        DoThi doThi = new DoThi("K");

        TrangThaiDoThi A = doThi.taoTrangThai("A");
        TrangThaiDoThi B = doThi.taoTrangThai("B");
        TrangThaiDoThi C = doThi.taoTrangThai("C");
        TrangThaiDoThi D = doThi.taoTrangThai("D");
        TrangThaiDoThi E = doThi.taoTrangThai("E");
        TrangThaiDoThi F = doThi.taoTrangThai("F");
        TrangThaiDoThi G = doThi.taoTrangThai("G");
        TrangThaiDoThi H = doThi.taoTrangThai("H");
        TrangThaiDoThi I = doThi.taoTrangThai("I");
        TrangThaiDoThi K = doThi.taoTrangThai("K");

        /*
         * Do thi khong co chu trinh, co do sau > 3.
         *
         * A -> B -> E -> I -> K
         * Duong di tu A den K co do sau 4.
         */
        doThi.themCung(A, B);
        doThi.themCung(A, C);
        doThi.themCung(A, D);

        doThi.themCung(B, E);
        doThi.themCung(B, F);

        doThi.themCung(C, G);
        doThi.themCung(D, H);

        doThi.themCung(E, I);
        doThi.themCung(I, K);

        System.out.println("BAI TAP 1: DO THI KHONG CO CHU TRINH, DO SAU > 3");
        System.out.println("Trang thai dau: A");
        System.out.println("Trang thai ket thuc: K");

        TimKiemRong bfs = new TimKiemRong();
        TrangThaiTimKiem ketQuaBFS = bfs.timKiem(A, true);
        TienIchTimKiem.inDuongDi(ketQuaBFS);

        doThi.reset();

        TimKiemSau dfs = new TimKiemSau();
        TrangThaiTimKiem ketQuaDFS = dfs.timKiem(A, true);
        TienIchTimKiem.inDuongDi(ketQuaDFS);
    }
}