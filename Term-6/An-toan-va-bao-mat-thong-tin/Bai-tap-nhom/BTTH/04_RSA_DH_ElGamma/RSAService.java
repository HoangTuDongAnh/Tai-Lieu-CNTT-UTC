public class RSAService
{
    private RSAUtil util;

    public RSAService()
    {
        util = new RSAUtil();
    }

    public RSAKeyPair taoKhoa(long p, long q, long e)
    {
        if (!util.isPrime(p) || !util.isPrime(q)) {
            throw new IllegalArgumentException("p va q phai la so nguyen to");
        }

        if (p == q) {
            throw new IllegalArgumentException("p va q phai khac nhau");
        }

        long n = p * q;
        long phi = (p - 1) * (q - 1);

        if (e <= 1 || e >= phi) {
            throw new IllegalArgumentException("e phai thoa 1 < e < phi(n)");
        }

        if (util.gcd(e, phi) != 1) {
            throw new IllegalArgumentException("gcd(e, phi(n)) phai = 1");
        }

        long d = util.modInverse(e, phi);

        return new RSAKeyPair(p, q, n, phi, e, d);
    }

    public long maHoaBaoMat(long M, RSAKeyPair keyPair)
    {
        if (M < 0 || M >= keyPair.getN()) {
            throw new IllegalArgumentException("Thong diep M phai thoa 0 <= M < n");
        }

        return util.powerMod(M, keyPair.getE(), keyPair.getN());
    }

    public long giaiMaBaoMat(long C, RSAKeyPair keyPair)
    {
        if (C < 0 || C >= keyPair.getN()) {
            throw new IllegalArgumentException("Ban ma C phai thoa 0 <= C < n");
        }

        return util.powerMod(C, keyPair.getD(), keyPair.getN());
    }

    public long kySo(long M, RSAKeyPair keyPair)
    {
        if (M < 0 || M >= keyPair.getN()) {
            throw new IllegalArgumentException("Thong diep M phai thoa 0 <= M < n");
        }

        return util.powerMod(M, keyPair.getD(), keyPair.getN());
    }

    public long xacMinhChuKy(long C, RSAKeyPair keyPair)
    {
        if (C < 0 || C >= keyPair.getN()) {
            throw new IllegalArgumentException("Gia tri chu ky C phai thoa 0 <= C < n");
        }

        return util.powerMod(C, keyPair.getE(), keyPair.getN());
    }
}