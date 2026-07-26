import java.util.*;

public class TimKiemRong {
    public TrangThaiTimKiem timKiem(TrangThaiTimKiem trangThaiDau) {
        return timKiem(trangThaiDau, false);
    }

    public TrangThaiTimKiem timKiem(TrangThaiTimKiem trangThaiDau, boolean inBang) {
        Queue<TrangThaiTimKiem> L = new LinkedList<>();
        Set<String> Q = new LinkedHashSet<>();

        trangThaiDau.setCha(null);
        trangThaiDau.setDoSau(0);

        L.add(trangThaiDau);
        Q.add(trangThaiDau.maTrangThai());

        int buoc = 0;

        if (inBang) {
            TienIchTimKiem.inTieuDeBang("TIM KIEM RONG BFS", "Danh sach L - Hang doi");
            TienIchTimKiem.inDongBang(buoc++, "", "", TienIchTimKiem.danhSachToString(L));
        }

        while (!L.isEmpty()) {
            TrangThaiTimKiem u = L.poll();

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

            for (TrangThaiTimKiem v : dsKe) {
                if (!Q.contains(v.maTrangThai())) {
                    v.setCha(u);
                    v.setDoSau(u.getDoSau() + 1);

                    L.add(v);
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