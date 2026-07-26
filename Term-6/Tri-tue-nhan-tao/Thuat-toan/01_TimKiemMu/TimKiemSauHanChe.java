import java.util.*;

public class TimKiemSauHanChe {
    public TrangThaiTimKiem timKiem(TrangThaiTimKiem trangThaiDau, int gioiHanDoSau) {
        return timKiem(trangThaiDau, gioiHanDoSau, false);
    }

    public TrangThaiTimKiem timKiem(
            TrangThaiTimKiem trangThaiDau,
            int gioiHanDoSau,
            boolean inBang
    ) {
        Deque<TrangThaiTimKiem> L = new ArrayDeque<>();
        Set<String> Q = new LinkedHashSet<>();

        trangThaiDau.setCha(null);
        trangThaiDau.setDoSau(0);

        L.push(trangThaiDau);
        Q.add(trangThaiDau.maTrangThai());

        int buoc = 0;

        if (inBang) {
            TienIchTimKiem.inTieuDeBang(
                    "TIM KIEM SAU HAN CHE DFS(d = " + gioiHanDoSau + ")",
                    "Danh sach L - Ngan xep"
            );

            TienIchTimKiem.inDongBang(
                    buoc++,
                    "",
                    "",
                    TienIchTimKiem.danhSachToString(L)
            );
        }

        while (!L.isEmpty()) {
            TrangThaiTimKiem u = L.pop();

            if (u.laTrangThaiDich()) {
                if (inBang) {
                    TienIchTimKiem.inDongBang(
                            buoc++,
                            u.maTrangThai(),
                            "Trang thai ket thuc",
                            "DUNG"
                    );
                }

                return u;
            }

            List<TrangThaiTimKiem> dsKe = u.sinhTrangThaiKe();

            for (int i = dsKe.size() - 1; i >= 0; i--) {
                TrangThaiTimKiem v = dsKe.get(i);
                int doSauMoi = u.getDoSau() + 1;

                if (doSauMoi <= gioiHanDoSau && !Q.contains(v.maTrangThai())) {
                    v.setCha(u);
                    v.setDoSau(doSauMoi);

                    L.push(v);
                    Q.add(v.maTrangThai());
                }
            }

            if (inBang) {
                TienIchTimKiem.inDongBang(
                        buoc++,
                        u.maTrangThai() + "(" + u.getDoSau() + ")",
                        TienIchTimKiem.danhSachToString(dsKe),
                        TienIchTimKiem.danhSachToString(L)
                );
            }
        }

        return null;
    }
}