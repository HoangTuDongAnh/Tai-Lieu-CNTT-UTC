public class KetQuaEuclidMoRong
{
    private long gcd;
    private long x;
    private long y;

    public KetQuaEuclidMoRong(long gcd, long x, long y)
    {
        this.gcd = gcd;
        this.x = x;
        this.y = y;
    }

    public long getGcd()
    {
        return gcd;
    }

    public long getX()
    {
        return x;
    }

    public long getY()
    {
        return y;
    }

    public String toString()
    {
        return "gcd = " + gcd + ", x = " + x + ", y = " + y;
    }
}