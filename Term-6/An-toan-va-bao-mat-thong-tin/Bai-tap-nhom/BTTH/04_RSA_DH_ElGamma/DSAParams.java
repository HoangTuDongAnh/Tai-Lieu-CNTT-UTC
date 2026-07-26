public class DSAParams
{
    private long hM;
    private long p;
    private long q;
    private long h;
    private long xA;
    private long k;

    public DSAParams(long hM, long p, long q, long h, long xA, long k)
    {
        this.hM = hM;
        this.p = p;
        this.q = q;
        this.h = h;
        this.xA = xA;
        this.k = k;
    }

    public long getHM()
    {
        return hM;
    }

    public long getP()
    {
        return p;
    }

    public long getQ()
    {
        return q;
    }

    public long getH()
    {
        return h;
    }

    public long getXA()
    {
        return xA;
    }

    public long getK()
    {
        return k;
    }
}