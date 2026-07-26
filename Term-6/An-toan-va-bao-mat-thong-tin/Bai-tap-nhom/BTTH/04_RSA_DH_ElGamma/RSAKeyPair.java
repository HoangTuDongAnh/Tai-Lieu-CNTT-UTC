public class RSAKeyPair
{
    private long p;
    private long q;
    private long n;
    private long phi;
    private long e;
    private long d;

    public RSAKeyPair(long p, long q, long n, long phi, long e, long d)
    {
        this.p = p;
        this.q = q;
        this.n = n;
        this.phi = phi;
        this.e = e;
        this.d = d;
    }

    public long getP()
    {
        return p;
    }

    public long getQ()
    {
        return q;
    }

    public long getN()
    {
        return n;
    }

    public long getPhi()
    {
        return phi;
    }

    public long getE()
    {
        return e;
    }

    public long getD()
    {
        return d;
    }

    public String getPublicKeyString()
    {
        return "{" + e + ", " + n + "}";
    }

    public String getPrivateKeyString()
    {
        return "{" + d + ", " + n + "}";
    }

    public String toString()
    {
        return "p = " + p
            + ", q = " + q
            + ", n = " + n
            + ", phi = " + phi
            + ", e = " + e
            + ", d = " + d;
    }
}