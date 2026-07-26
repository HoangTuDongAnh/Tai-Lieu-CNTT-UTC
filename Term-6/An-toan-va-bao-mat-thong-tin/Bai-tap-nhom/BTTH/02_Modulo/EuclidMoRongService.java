public class EuclidMoRongService
{
    public KetQuaEuclidMoRong solve(long a, long b)
    {
        long oldR = a;
        long r = b;

        long oldS = 1;
        long s = 0;

        long oldT = 0;
        long t = 1;

        while (r != 0) {
            long q = oldR / r;

            long tempR = oldR - q * r;
            oldR = r;
            r = tempR;

            long tempS = oldS - q * s;
            oldS = s;
            s = tempS;

            long tempT = oldT - q * t;
            oldT = t;
            t = tempT;
        }

        return new KetQuaEuclidMoRong(oldR, oldS, oldT);
    }
}