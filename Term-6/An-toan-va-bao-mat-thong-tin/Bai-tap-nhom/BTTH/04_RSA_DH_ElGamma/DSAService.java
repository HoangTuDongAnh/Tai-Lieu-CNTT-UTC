public class DSAService
{
    private DSAUtil util;

    public DSAService()
    {
        util = new DSAUtil();
    }

    public long tinhG(long p, long q, long h)
    {
        return util.powerMod(h, (p - 1) / q, p);
    }

    public long tinhKhoaCongKhai(long p, long g, long xA)
    {
        return util.powerMod(g, xA, p);
    }

    public DSASignature kySo(long hM, long p, long q, long g, long xA, long k)
    {
        if (k <= 0 || k >= q) {
            throw new IllegalArgumentException("k phai thoa 0 < k < q");
        }

        if (util.gcd(k, q) != 1) {
            throw new IllegalArgumentException("k phai nguyen to cung nhau voi q");
        }

        long r = util.powerMod(g, k, p) % q;
        long kInverse = util.modInverse(k, q);
        long s = util.mod(kInverse * (hM + xA * r), q);

        return new DSASignature(r, s);
    }

    public long tinhW(long s, long q)
    {
        return util.modInverse(s, q);
    }

    public long tinhU1(long hM, long w, long q)
    {
        return util.mod(hM * w, q);
    }

    public long tinhU2(long r, long w, long q)
    {
        return util.mod(r * w, q);
    }

    public long tinhV(long p, long q, long g, long yA, long u1, long u2)
    {
        long part1 = util.powerMod(g, u1, p);
        long part2 = util.powerMod(yA, u2, p);
        long value = (part1 * part2) % p;
        return value % q;
    }

    public boolean xacMinh(long hM, long p, long q, long g, long yA, DSASignature signature)
    {
        long w = tinhW(signature.getS(), q);
        long u1 = tinhU1(hM, w, q);
        long u2 = tinhU2(signature.getR(), w, q);
        long v = tinhV(p, q, g, yA, u1, u2);

        return v == signature.getR();
    }
}