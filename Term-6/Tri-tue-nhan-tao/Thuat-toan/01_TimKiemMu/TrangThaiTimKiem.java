import java.util.List;

public interface TrangThaiTimKiem {
    String maTrangThai();

    boolean laTrangThaiDich();

    List<TrangThaiTimKiem> sinhTrangThaiKe();

    TrangThaiTimKiem getCha();

    void setCha(TrangThaiTimKiem cha);

    int getDoSau();

    void setDoSau(int doSau);
}