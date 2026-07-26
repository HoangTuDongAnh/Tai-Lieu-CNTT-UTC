public class AESUtil
{
    public int[] hexToBytes(String hex)
    {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Chuoi hex phai co do dai chan");
        }

        int[] bytes = new int[hex.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            String part = hex.substring(i * 2, i * 2 + 2);
            bytes[i] = Integer.parseInt(part, 16);
        }

        return bytes;
    }

    public String bytesToHex(int[] bytes)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String h = Integer.toHexString(bytes[i] & 0xFF).toUpperCase();
            if (h.length() == 1) {
                sb.append("0");
            }
            sb.append(h);
        }

        return sb.toString();
    }

    public int[][] bytesToState(int[] bytes)
    {
        int[][] state = new int[4][4];

        for (int c = 0; c < 4; c++) {
            for (int r = 0; r < 4; r++) {
                state[r][c] = bytes[c * 4 + r];
            }
        }

        return state;
    }

    public int[] stateToBytes(int[][] state)
    {
        int[] bytes = new int[16];

        for (int c = 0; c < 4; c++) {
            for (int r = 0; r < 4; r++) {
                bytes[c * 4 + r] = state[r][c] & 0xFF;
            }
        }

        return bytes;
    }

    public int[][] copyState(int[][] state)
    {
        int[][] result = new int[4][4];

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                result[r][c] = state[r][c];
            }
        }

        return result;
    }

    public int gfMul(int a, int b)
    {
        int result = 0;
        int aa = a & 0xFF;
        int bb = b & 0xFF;

        while (bb > 0) {
            if ((bb & 1) != 0) {
                result ^= aa;
            }

            boolean highBit = (aa & 0x80) != 0;
            aa = (aa << 1) & 0xFF;

            if (highBit) {
                aa ^= 0x1B;
            }

            bb >>= 1;
        }

        return result & 0xFF;
    }

    public void printState(String title, int[][] state)
    {
        System.out.println(title);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                String h = Integer.toHexString(state[r][c] & 0xFF).toUpperCase();
                if (h.length() == 1) {
                    h = "0" + h;
                }
                System.out.print(h + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}