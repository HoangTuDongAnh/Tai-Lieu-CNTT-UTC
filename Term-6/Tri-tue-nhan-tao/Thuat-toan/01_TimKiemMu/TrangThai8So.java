import java.util.*;

public class TrangThai8So implements TrangThaiTimKiem {
    private String banCo;
    private TrangThaiTimKiem cha;
    private int doSau;

    private static final String DICH = "1238_4765";

    public TrangThai8So(String banCo) {
        this.banCo = banCo;
        this.cha = null;
        this.doSau = 0;
    }

    @Override
    public List<TrangThaiTimKiem> sinhTrangThaiKe() {
        List<TrangThaiTimKiem> ketQua = new ArrayList<>();

        /*
         * Thu tu toan tu:
         * Up, Left, Right, Down
         */
        themNeuHopLe(ketQua, -3); // Up
        themNeuHopLe(ketQua, -1); // Left
        themNeuHopLe(ketQua, 1);  // Right
        themNeuHopLe(ketQua, 3);  // Down

        return ketQua;
    }

    private void themNeuHopLe(List<TrangThaiTimKiem> ketQua, int huong) {
        int viTriTrong = banCo.indexOf('_');
        int viTriMoi = viTriTrong + huong;

        if (!diChuyenHopLe(viTriTrong, viTriMoi, huong)) {
            return;
        }

        char[] arr = banCo.toCharArray();

        char temp = arr[viTriTrong];
        arr[viTriTrong] = arr[viTriMoi];
        arr[viTriMoi] = temp;

        ketQua.add(new TrangThai8So(new String(arr)));
    }

    private boolean diChuyenHopLe(int viTriTrong, int viTriMoi, int huong) {
        if (viTriMoi < 0 || viTriMoi >= 9) {
            return false;
        }

        int cotCu = viTriTrong % 3;

        if (huong == -1 && cotCu == 0) {
            return false;
        }

        if (huong == 1 && cotCu == 2) {
            return false;
        }

        return true;
    }

    @Override
    public String maTrangThai() {
        return banCo;
    }

    @Override
    public boolean laTrangThaiDich() {
        return banCo.equals(DICH);
    }

    @Override
    public TrangThaiTimKiem getCha() {
        return cha;
    }

    @Override
    public void setCha(TrangThaiTimKiem cha) {
        this.cha = cha;
    }

    @Override
    public int getDoSau() {
        return doSau;
    }

    @Override
    public void setDoSau(int doSau) {
        this.doSau = doSau;
    }

    public static void inBanCo(String s) {
        System.out.println(s.charAt(0) + " " + s.charAt(1) + " " + s.charAt(2));
        System.out.println(s.charAt(3) + " " + s.charAt(4) + " " + s.charAt(5));
        System.out.println(s.charAt(6) + " " + s.charAt(7) + " " + s.charAt(8));
    }
}