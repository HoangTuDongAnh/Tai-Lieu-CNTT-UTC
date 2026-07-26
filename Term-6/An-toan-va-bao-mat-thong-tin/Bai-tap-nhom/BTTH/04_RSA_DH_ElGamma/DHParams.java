public class DHParams
{
    private long q;
    private long a;
    private long xA;
    private long xB;

    public DHParams(long q, long a, long xA, long xB)
    {
        this.q = q;
        this.a = a;
        this.xA = xA;
        this.xB = xB;
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

    public long getXB()
    {
        return xB;
    }
}