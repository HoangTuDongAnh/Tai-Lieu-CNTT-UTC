import java.util.*;

public class DoThi {
    private Map<String, TrangThaiDoThi> cacTrangThai;
    private String tenTrangThaiDich;

    public DoThi(String tenTrangThaiDich) {
        this.tenTrangThaiDich = tenTrangThaiDich;
        this.cacTrangThai = new LinkedHashMap<>();
    }

    public TrangThaiDoThi taoTrangThai(String ten) {
        TrangThaiDoThi trangThai = new TrangThaiDoThi(ten, tenTrangThaiDich);
        cacTrangThai.put(ten, trangThai);
        return trangThai;
    }

    public void themCung(TrangThaiDoThi u, TrangThaiDoThi v) {
        u.themTrangThaiKe(v);
    }

    public void reset() {
        for (TrangThaiDoThi t : cacTrangThai.values()) {
            t.setCha(null);
            t.setDoSau(0);
        }
    }
}