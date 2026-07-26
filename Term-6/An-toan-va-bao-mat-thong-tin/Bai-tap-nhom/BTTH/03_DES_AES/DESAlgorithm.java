public class DESAlgorithm
{
    private DESUtil util;
    private DESKeySchedule keySchedule;

    public DESAlgorithm()
    {
        util = new DESUtil();
        keySchedule = new DESKeySchedule();
    }

    public String ip(String x64)
    {
        return util.permute(x64, DESConst.IP);
    }

    public String ipInverse(String x64)
    {
        return util.permute(x64, DESConst.IP_INV);
    }

    public String[] splitBlock64(String x64)
    {
        return util.splitInHalf(x64);
    }

    public String expandE(String r32)
    {
        return util.permute(r32, DESConst.E);
    }

    public String xor48(String a48, String b48)
    {
        return util.xorBits(a48, b48);
    }

    public String permuteP(String x32)
    {
        return util.permute(x32, DESConst.P);
    }

    public String substitute(String x48)
    {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            String block6 = x48.substring(i * 6, i * 6 + 6);

            String rowBits = "" + block6.charAt(0) + block6.charAt(5);
            String colBits = block6.substring(1, 5);

            int row = Integer.parseInt(rowBits, 2);
            int col = Integer.parseInt(colBits, 2);

            int value = DESConst.S_BOX[i][row][col];
            result.append(util.to4BitBinary(value));
        }

        return result.toString();
    }

    public String feistel(String r32, String subKey48)
    {
        String expanded = expandE(r32);
        String xored = xor48(expanded, subKey48);
        String sboxed = substitute(xored);
        return permuteP(sboxed);
    }

    public String encryptBlockBinary(String plain64, String key64)
    {
        String ipResult = ip(plain64);
        String[] lr = splitBlock64(ipResult);

        String l = lr[0];
        String r = lr[1];

        String[] subKeys = keySchedule.generateSubKeys(key64);

        for (int i = 0; i < 16; i++) {
            String oldL = l;
            String oldR = r;

            l = oldR;
            String f = feistel(oldR, subKeys[i]);
            r = util.xorBits(oldL, f);
        }

        String preOutput = r + l;
        return ipInverse(preOutput);
    }

    public String encryptHex(String plainHex, String keyHex)
    {
        String plain64 = util.hexToBin(plainHex);
        String key64 = util.hexToBin(keyHex);
        String cipher64 = encryptBlockBinary(plain64, key64);
        return util.binToHex(cipher64);
    }

    public String[] getSubKeysFromHex(String keyHex)
    {
        String key64 = util.hexToBin(keyHex);
        return keySchedule.generateSubKeys(key64);
    }
    
    public void printDecryptRoundInfo(String cipherHex, String keyHex, int round)
{
    if (round < 1 || round > 16) {
        throw new IllegalArgumentException("i phai nam trong [1..16]");
    }

    String cipher64 = util.hexToBin(cipherHex);
    String key64 = util.hexToBin(keyHex);

    String ipResult = ip(cipher64);
    String[] lr = splitBlock64(ipResult);

    String l = lr[0];
    String r = lr[1];

    String[] subKeys = keySchedule.generateSubKeys(key64);

    for (int i = 1; i <= 16; i++) {
        String kDecrypt = subKeys[16 - i];

        String oldL = l;
        String oldR = r;

        l = oldR;
        String f = feistel(oldR, kDecrypt);
        r = util.xorBits(oldL, f);

        if (i == round) {
            System.out.println("=== VONG GIAI MA " + i + " ===");
            System.out.println("Ki = " + kDecrypt);
            System.out.println("Li = " + l);
            System.out.println("Ri = " + r);
            System.out.println("Ki (hex) = " + util.binToHex(kDecrypt));
            System.out.println("Li (hex) = " + util.binToHex(l));
            System.out.println("Ri (hex) = " + util.binToHex(r));
            return;
        }
    }
}
}