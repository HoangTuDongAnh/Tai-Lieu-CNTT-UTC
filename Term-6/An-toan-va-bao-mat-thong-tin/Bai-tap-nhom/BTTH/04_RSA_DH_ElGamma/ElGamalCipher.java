public class ElGamalCipher
{
    private long c1;
    private long c2;

    public ElGamalCipher(long c1, long c2)
    {
        this.c1 = c1;
        this.c2 = c2;
    }

    public long getC1()
    {
        return c1;
    }

    public long getC2()
    {
        return c2;
    }

    public String toString()
    {
        return "(" + c1 + ", " + c2 + ")";
    }
}