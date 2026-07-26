public class ElGamalService
{
    private ElGamalUtil util;

    public ElGamalService()
    {
        util = new ElGamalUtil();
    }

    public long tinhKhoaCongKhai(long q, long a, long xA)
    {
        return util.powerMod(a, xA, q);
    }

    public ElGamalCipher maHoa(long q, long a, long yA, long k, long M)
    {
        if (M < 0 || M >= q) {
            throw new IllegalArgumentException("Thong diep M phai thoa 0 <= M < q");
        }

        long K = util.powerMod(yA, k, q);
        long c1 = util.powerMod(a, k, q);
        long c2 = (K * M) % q;

        return new ElGamalCipher(c1, c2);
    }

    public long giaiMa(long q, long xA, ElGamalCipher cipher)
    {
        long K = util.powerMod(cipher.getC1(), xA, q);
        long KInverse = util.modInverse(K, q);
        return (cipher.getC2() * KInverse) % q;
    }

    public long tinhKMaHoa(long q, long yA, long k)
    {
        return util.powerMod(yA, k, q);
    }

    public long tinhKGiaiMa(long q, long c1, long xA)
    {
        return util.powerMod(c1, xA, q);
    }
}