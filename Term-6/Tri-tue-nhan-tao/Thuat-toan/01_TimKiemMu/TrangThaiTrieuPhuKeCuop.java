import java.util.*;

public class TrangThaiTrieuPhuKeCuop implements TrangThaiTimKiem {
    private int a;
    private int b;
    private int k;

    private TrangThaiTimKiem cha;
    private int doSau;

    public TrangThaiTrieuPhuKeCuop(int a, int b, int k) {
        this.a = a;
        this.b = b;
        this.k = k;
        this.cha = null;
        this.doSau = 0;
    }

    @Override
    public List<TrangThaiTimKiem> sinhTrangThaiKe() {
        List<TrangThaiTimKiem> ketQua = new ArrayList<>();

        /*
         * Cac toan tu:
         * {0,1}: thuyen cho 1 ke cuop
         * {1,0}: thuyen cho 1 trieu phu
         * {1,1}: thuyen cho 1 trieu phu va 1 ke cuop
         * {0,2}: thuyen cho 2 ke cuop
         * {2,0}: thuyen cho 2 trieu phu
         */
        int[][] toanTu = {
                {0, 1},
                {1, 0},
                {1, 1},
                {0, 2},
                {2, 0}
        };

        for (int[] tt : toanTu) {
            TrangThaiTrieuPhuKeCuop moi = diChuyen(tt[0], tt[1]);

            if (moi.hopLe()) {
                ketQua.add(moi);
            }
        }

        return ketQua;
    }

    private TrangThaiTrieuPhuKeCuop diChuyen(int soTrieuPhu, int soKeCuop) {
        if (k == 1) {
            return new TrangThaiTrieuPhuKeCuop(
                    a - soTrieuPhu,
                    b - soKeCuop,
                    0
            );
        }

        return new TrangThaiTrieuPhuKeCuop(
                a + soTrieuPhu,
                b + soKeCuop,
                1
        );
    }

    private boolean hopLe() {
        if (a < 0 || a > 3 || b < 0 || b > 3) {
            return false;
        }

        int trieuPhuTrai = a;
        int keCuopTrai = b;

        int trieuPhuPhai = 3 - a;
        int keCuopPhai = 3 - b;

        if (trieuPhuTrai > 0 && keCuopTrai > trieuPhuTrai) {
            return false;
        }

        if (trieuPhuPhai > 0 && keCuopPhai > trieuPhuPhai) {
            return false;
        }

        return true;
    }

    @Override
    public String maTrangThai() {
        return "(" + a + "," + b + "," + k + ")";
    }

    @Override
    public boolean laTrangThaiDich() {
        return a == 0 && b == 0 && k == 0;
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
}