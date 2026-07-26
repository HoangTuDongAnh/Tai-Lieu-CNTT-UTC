public class ElGamalParams
{
    private long q;
    private long a;
    private long xA;
    private long k;
    private long M;

    public ElGamalParams(long q, long a, long xA, long k, long M)
    {
        this.q = q;
        this.a = a;
        this.xA = xA;
        this.k = k;
        this.M = M;
    }

    public long getQ()
    {
        return q;
    }

    public long getA()
    {
        return a;
    }

    public long getXA()
    {
        return xA;
    }

    public long getK()
    {
        return k;
    }

    public long getM()
    {
        return M;
    }
}