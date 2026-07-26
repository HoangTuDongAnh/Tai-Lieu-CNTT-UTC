public class DSASignature
{
    private long r;
    private long s;

    public DSASignature(long r, long s)
    {
        this.r = r;
        this.s = s;
    }

    public long getR()
    {
        return r;
    }

    public long getS()
    {
        return s;
    }

    public String toString()
    {
        return "(" + r + ", " + s + ")";
    }
}