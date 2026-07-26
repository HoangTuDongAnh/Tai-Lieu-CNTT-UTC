public class DESKeySchedule
{
    private DESUtil util;

    public DESKeySchedule()
    {
        util = new DESUtil();
    }

    public String pc1(String key64)
    {
        return util.permute(key64, DESConst.PC1);
    }

    public String[] splitKey56(String key56)
    {
        return util.splitInHalf(key56);
    }

    public String shiftLeft28(String input28, int s)
    {
        return util.leftRotate(input28, s);
    }

    public String pc2(String c28, String d28)
    {
        String cd56 = c28 + d28;
        return util.permute(cd56, DESConst.PC2);
    }

    public String[] generateSubKeys(String key64)
    {
        String key56 = pc1(key64);
        String[] cd = splitKey56(key56);

        String c = cd[0];
        String d = cd[1];

        String[] subKeys = new String[16];

        for (int i = 0; i < 16; i++) {
            c = shiftLeft28(c, DESConst.SHIFTS[i]);
            d = shiftLeft28(d, DESConst.SHIFTS[i]);
            subKeys[i] = pc2(c, d);
        }

        return subKeys;
    }
}