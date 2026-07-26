public class DESUtil
{
    public String hexToBin(String hex)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < hex.length(); i++) {
            char c = Character.toUpperCase(hex.charAt(i));
            switch (c) {
                case '0': sb.append("0000"); break;
                case '1': sb.append("0001"); break;
                case '2': sb.append("0010"); break;
                case '3': sb.append("0011"); break;
                case '4': sb.append("0100"); break;
                case '5': sb.append("0101"); break;
                case '6': sb.append("0110"); break;
                case '7': sb.append("0111"); break;
                case '8': sb.append("1000"); break;
                case '9': sb.append("1001"); break;
                case 'A': sb.append("1010"); break;
                case 'B': sb.append("1011"); break;
                case 'C': sb.append("1100"); break;
                case 'D': sb.append("1101"); break;
                case 'E': sb.append("1110"); break;
                case 'F': sb.append("1111"); break;
                default:
                    throw new IllegalArgumentException("Ky tu hex khong hop le: " + c);
            }
        }

        return sb.toString();
    }

    public String binToHex(String bin)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bin.length(); i += 4) {
            String block = bin.substring(i, i + 4);

            if (block.equals("0000")) sb.append("0");
            else if (block.equals("0001")) sb.append("1");
            else if (block.equals("0010")) sb.append("2");
            else if (block.equals("0011")) sb.append("3");
            else if (block.equals("0100")) sb.append("4");
            else if (block.equals("0101")) sb.append("5");
            else if (block.equals("0110")) sb.append("6");
            else if (block.equals("0111")) sb.append("7");
            else if (block.equals("1000")) sb.append("8");
            else if (block.equals("1001")) sb.append("9");
            else if (block.equals("1010")) sb.append("A");
            else if (block.equals("1011")) sb.append("B");
            else if (block.equals("1100")) sb.append("C");
            else if (block.equals("1101")) sb.append("D");
            else if (block.equals("1110")) sb.append("E");
            else if (block.equals("1111")) sb.append("F");
            else throw new IllegalArgumentException("Block bit khong hop le: " + block);
        }

        return sb.toString();
    }

    public String permute(String input, int[] table)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < table.length; i++) {
            sb.append(input.charAt(table[i] - 1));
        }

        return sb.toString();
    }

    public String xorBits(String a, String b)
    {
        if (a.length() != b.length()) {
            throw new IllegalArgumentException("Hai chuoi bit phai cung do dai");
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == b.charAt(i)) {
                sb.append('0');
            } else {
                sb.append('1');
            }
        }

        return sb.toString();
    }

    public String[] splitInHalf(String input)
    {
        int mid = input.length() / 2;
        String[] result = new String[2];
        result[0] = input.substring(0, mid);
        result[1] = input.substring(mid);
        return result;
    }

    public String leftRotate(String input, int shift)
    {
        shift = shift % input.length();
        return input.substring(shift) + input.substring(0, shift);
    }

    public String to4BitBinary(int value)
    {
        String bin = Integer.toBinaryString(value);
        while (bin.length() < 4) {
            bin = "0" + bin;
        }
        return bin;
    }
}