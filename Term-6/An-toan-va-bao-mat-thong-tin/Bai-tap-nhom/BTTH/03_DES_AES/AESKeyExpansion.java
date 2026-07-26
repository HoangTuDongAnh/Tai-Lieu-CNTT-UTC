public class AESKeyExpansion
{
    public int[] keyExpansion(int[] key16)
    {
        if (key16.length != 16) {
            throw new IllegalArgumentException("Khoa AES-128 phai co 16 byte");
        }

        int[] expanded = new int[176];

        for (int i = 0; i < 16; i++) {
            expanded[i] = key16[i];
        }

        int bytesGenerated = 16;
        int rconIndex = 1;
        int[] temp = new int[4];

        while (bytesGenerated < 176) {
            for (int i = 0; i < 4; i++) {
                temp[i] = expanded[bytesGenerated - 4 + i];
            }

            if (bytesGenerated % 16 == 0) {
                temp = rotWord(temp);
                temp = subWord(temp);
                temp[0] = temp[0] ^ AESConst.RCON[rconIndex];
                rconIndex++;
            }

            for (int i = 0; i < 4; i++) {
                expanded[bytesGenerated] = (expanded[bytesGenerated - 16] ^ temp[i]) & 0xFF;
                bytesGenerated++;
            }
        }

        return expanded;
    }

    public int[] rotWord(int[] word)
    {
        int[] result = new int[4];
        result[0] = word[1];
        result[1] = word[2];
        result[2] = word[3];
        result[3] = word[0];
        return result;
    }

    public int[] subWord(int[] word)
    {
        int[] result = new int[4];

        for (int i = 0; i < 4; i++) {
            result[i] = AESConst.S_BOX[word[i] & 0xFF];
        }

        return result;
    }

    public int[][] getRoundKeyState(int[] expanded, int round)
    {
        int[] roundBytes = new int[16];
        int start = round * 16;

        for (int i = 0; i < 16; i++) {
            roundBytes[i] = expanded[start + i];
        }

        AESUtil util = new AESUtil();
        return util.bytesToState(roundBytes);
    }
}