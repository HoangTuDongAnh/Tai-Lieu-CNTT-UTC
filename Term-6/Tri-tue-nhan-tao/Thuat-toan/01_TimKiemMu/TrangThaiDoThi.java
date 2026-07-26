import java.util.*;

public class TrangThaiDoThi implements TrangThaiTimKiem {
    private String ten;
    private String tenTrangThaiDich;
    private List<TrangThaiTimKiem> danhSachKe;
    private TrangThaiTimKiem cha;
    private int doSau;

    public TrangThaiDoThi(String ten, String tenTrangThaiDich) {
        this.ten = ten;
        this.tenTrangThaiDich = tenTrangThaiDich;
        this.danhSachKe = new ArrayList<>();
        this.cha = null;
        this.doSau = 0;
    }

    public void themTrangThaiKe(TrangThaiDoThi v) {
        danhSachKe.add(v);
    }

    @Override
    public String maTrangThai() {
        return ten;
    }

    @Override
    public boolean laTrangThaiDich() {
        return ten.equals(tenTrangThaiDich);
    }

    @Override
    public List<TrangThaiTimKiem> sinhTrangThaiKe() {
        return danhSachKe;
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