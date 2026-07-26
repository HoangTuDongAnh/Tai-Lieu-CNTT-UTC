import java.util.*;

public class TienIchTimKiem {
    public static String danhSachToString(Collection<TrangThaiTimKiem> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (TrangThaiTimKiem t : danhSach) {
            sb.append(t.maTrangThai()).append(" ");
        }

        return sb.toString().trim();
    }

    public static void inTieuDeBang(String tenThuatToan, String tenDanhSachL) {
        System.out.println("\n============================================================");
        System.out.println(tenThuatToan);
        System.out.println("============================================================");
        System.out.printf("%-6s %-22s %-55s %-55s%n",
                "Buoc", "Phat trien u", "Trang thai ke v", tenDanhSachL);
    }

    public static void inDongBang(int buoc, String u, String ke, String l) {
        System.out.printf("%-6d %-22s %-55s %-55s%n", buoc, u, ke, l);
    }

    public static void inDuongDi(TrangThaiTimKiem ketQua) {
        if (ketQua == null) {
            System.out.println("\nKhong tim thay loi giai!");
            return;
        }

        Stack<TrangThaiTimKiem> duongDi = new Stack<>();
        TrangThaiTimKiem hienTai = ketQua;

        while (hienTai != null) {
            duongDi.push(hienTai);
            hienTai = hienTai.getCha();
        }

        System.out.println("\nDuong di loi giai:");

        int buoc = 0;

        while (!duongDi.isEmpty()) {
            TrangThaiTimKiem t = duongDi.pop();
            System.out.println("Buoc " + buoc + ": " + t.maTrangThai());
            buoc++;
        }

        System.out.println("Tong so buoc di: " + (buoc - 1));
    }
}