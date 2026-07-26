import java.util.*;

public class TimKiemSau {
    public TrangThaiTimKiem timKiem(TrangThaiTimKiem trangThaiDau) {
        return timKiem(trangThaiDau, false);
    }

    public TrangThaiTimKiem timKiem(TrangThaiTimKiem trangThaiDau, boolean inBang) {
        Deque<TrangThaiTimKiem> L = new ArrayDeque<>();
        Set<String> Q = new LinkedHashSet<>();

        trangThaiDau.setCha(null);
        trangThaiDau.setDoSau(0);

        L.push(trangThaiDau);
        Q.add(trangThaiDau.maTrangThai());

        int buoc = 0;

        if (inBang) {
            TienIchTimKiem.inTieuDeBang("TIM KIEM SAU DFS", "Danh sach L - Ngan xep");
            TienIchTimKiem.inDongBang(buoc++, "", "", TienIchTimKiem.danhSachToString(L));
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

            /*
             * DFS dung ngan xep.
             * De trang thai ke dau tien duoc phat trien truoc,
             * ta dua cac trang thai vao stack theo thu tu nguoc.
             */
            for (int i = dsKe.size() - 1; i >= 0; i--) {
                TrangThaiTimKiem v = dsKe.get(i);

                if (!Q.contains(v.maTrangThai())) {
                    v.setCha(u);
                    v.setDoSau(u.getDoSau() + 1);

                    L.push(v);
                    Q.add(v.maTrangThai());
                }
            }

            if (inBang) {
                TienIchTimKiem.inDongBang(
                        buoc++,
                        u.maTrangThai(),
                        TienIchTimKiem.danhSachToString(dsKe),
                        TienIchTimKiem.danhSachToString(L)
                );
            }
        }

        return null;
    }
}