public class BaiTap2_CoChuTrinh {
    public static void main(String[] args) {
        DoThi doThi = new DoThi("G");

        TrangThaiDoThi A = doThi.taoTrangThai("A");
        TrangThaiDoThi B = doThi.taoTrangThai("B");
        TrangThaiDoThi C = doThi.taoTrangThai("C");
        TrangThaiDoThi D = doThi.taoTrangThai("D");
        TrangThaiDoThi E = doThi.taoTrangThai("E");
        TrangThaiDoThi F = doThi.taoTrangThai("F");
        TrangThaiDoThi G = doThi.taoTrangThai("G");

        /*
         * Do thi co chu trinh, co do sau > 3.
         *
         * Nhanh co loi giai:
         * A -> B -> D -> G
         *
         * Nhanh co chu trinh:
         * C -> F -> E -> C
         */
        doThi.themCung(A, B);
        doThi.themCung(A, C);

        doThi.themCung(B, D);
        doThi.themCung(D, G);

        doThi.themCung(C, F);
        doThi.themCung(F, E);
        doThi.themCung(E, C);

        int gioiHanDoSau = 3;

        System.out.println("BAI TAP 2: DO THI CO CHU TRINH, DO SAU > 3");
        System.out.println("Trang thai dau: A");
        System.out.println("Trang thai ket thuc: G");
        System.out.println("Gioi han do sau: " + gioiHanDoSau);

        TimKiemSauHanChe dfsHanChe = new TimKiemSauHanChe();
        TrangThaiTimKiem ketQua = dfsHanChe.timKiem(A, gioiHanDoSau, true);
        TienIchTimKiem.inDuongDi(ketQua);
    }
}