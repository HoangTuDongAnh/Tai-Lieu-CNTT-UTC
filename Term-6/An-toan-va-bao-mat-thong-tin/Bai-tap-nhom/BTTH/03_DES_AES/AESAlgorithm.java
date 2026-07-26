public class AESAlgorithm
{
    private AESUtil util;
    private AESKeyExpansion keyExpansion;

    public AESAlgorithm()
    {
        util = new AESUtil();
        keyExpansion = new AESKeyExpansion();
    }

    public int[][] subBytes(int[][] state)
    {
        int[][] result = util.copyState(state);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                result[r][c] = AESConst.S_BOX[result[r][c] & 0xFF];
            }
        }

        return result;
    }

    public int[][] shiftRows(int[][] state)
    {
        int[][] result = util.copyState(state);

        for (int r = 1; r < 4; r++) {
            int[] row = new int[4];

            for (int c = 0; c < 4; c++) {
                row[c] = result[r][c];
            }

            for (int c = 0; c < 4; c++) {
                result[r][c] = row[(c + r) % 4];
            }
        }

        return result;
    }

    public int[][] mixColumns(int[][] state)
    {
        int[][] result = new int[4][4];

        for (int c = 0; c < 4; c++) {
            int s0 = state[0][c];
            int s1 = state[1][c];
            int s2 = state[2][c];
            int s3 = state[3][c];

            result[0][c] = util.gfMul(0x02, s0) ^ util.gfMul(0x03, s1) ^ s2 ^ s3;
            result[1][c] = s0 ^ util.gfMul(0x02, s1) ^ util.gfMul(0x03, s2) ^ s3;
            result[2][c] = s0 ^ s1 ^ util.gfMul(0x02, s2) ^ util.gfMul(0x03, s3);
            result[3][c] = util.gfMul(0x03, s0) ^ s1 ^ s2 ^ util.gfMul(0x02, s3);

            result[0][c] &= 0xFF;
            result[1][c] &= 0xFF;
            result[2][c] &= 0xFF;
            result[3][c] &= 0xFF;
        }

        return result;
    }

    public int[][] addRoundKey(int[][] state, int[][] roundKey)
    {
        int[][] result = new int[4][4];

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                result[r][c] = (state[r][c] ^ roundKey[r][c]) & 0xFF;
            }
        }

        return result;
    }

    public String encryptHex(String plainHex, String keyHex)
    {
        int[] plainBytes = util.hexToBytes(plainHex);
        int[] keyBytes = util.hexToBytes(keyHex);

        if (plainBytes.length != 16) {
            throw new IllegalArgumentException("Input AES phai co 16 byte");
        }
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("Khoa AES-128 phai co 16 byte");
        }

        int[] expanded = keyExpansion.keyExpansion(keyBytes);

        int[][] state = util.bytesToState(plainBytes);

        int[][] roundKey0 = keyExpansion.getRoundKeyState(expanded, 0);
        state = addRoundKey(state, roundKey0);

        for (int round = 1; round <= 9; round++) {
            state = subBytes(state);
            state = shiftRows(state);
            state = mixColumns(state);

            int[][] roundKey = keyExpansion.getRoundKeyState(expanded, round);
            state = addRoundKey(state, roundKey);
        }

        state = subBytes(state);
        state = shiftRows(state);

        int[][] roundKey10 = keyExpansion.getRoundKeyState(expanded, 10);
        state = addRoundKey(state, roundKey10);

        int[] cipherBytes = util.stateToBytes(state);
        return util.bytesToHex(cipherBytes);
    }

    public int[] expandKeyHex(String keyHex)
    {
        int[] keyBytes = util.hexToBytes(keyHex);
        return keyExpansion.keyExpansion(keyBytes);
    }

    public int[][] getRoundKeyStateFromExpanded(int[] expanded, int round)
    {
        return keyExpansion.getRoundKeyState(expanded, round);
    }
}