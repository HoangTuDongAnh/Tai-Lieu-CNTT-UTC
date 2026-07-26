public class DHService
{
    private DHUtil util;

    public DHService()
    {
        util = new DHUtil();
    }

    public long tinhKhoaCongKhai(long a, long x, long q)
    {
        return util.powerMod(a, x, q);
    }

    public long tinhKhoaPhien(long yOther, long xSelf, long q)
    {
        return util.powerMod(yOther, xSelf, q);
    }
}