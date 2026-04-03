public class RowTransCipher {

    static String normalizeText(String text) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = Character.toUpperCase(text.charAt(i));
            if (ch >= 'A' && ch <= 'Z') {
                output.append(ch);
            }
        }

        return output.toString();
    }

    static boolean validateKey(String key) {
        int n = key.length();
        boolean[] seen = new boolean[n + 1];

        for (int i = 0; i < n; i++) {
            char ch = key.charAt(i);

            if (ch < '1' || ch > '9') return false;

            int value = ch - '0';
            if (value < 1 || value > n) return false;
            if (seen[value]) return false;

            seen[value] = true;
        }

        return true;
    }

    static int[] buildColumnReadOrder(String key) {
        if (!validateKey(key)) {
            throw new IllegalArgumentException("Key khong hop le");
        }

        int n = key.length();
        int[] order = new int[n];

        for (int number = 1; number <= n; number++) {
            for (int i = 0; i < n; i++) {
                if (key.charAt(i) - '0' == number) {
                    order[number - 1] = i;
                    break;
                }
            }
        }

        return order;
    }

    static String padWithX(String text, int columnCount) {
        StringBuilder output = new StringBuilder(text);

        while (output.length() % columnCount != 0) {
            output.append('X');
        }

        return output.toString();
    }

    static char[][] buildGrid(String text, int columnCount) {
        int rowCount = text.length() / columnCount;
        char[][] grid = new char[rowCount][columnCount];

        int index = 0;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                grid[i][j] = text.charAt(index++);
            }
        }

        return grid;
    }

    static String encrypt(String message, String key) {
        message = normalizeText(message);

        int columnCount = key.length();
        message = padWithX(message, columnCount);

        char[][] grid = buildGrid(message, columnCount);
        int[] columnReadOrder = buildColumnReadOrder(key);

        StringBuilder output = new StringBuilder();

        for (int k = 0; k < columnReadOrder.length; k++) {
            int columnIndex = columnReadOrder[k];
            for (int i = 0; i < grid.length; i++) {
                output.append(grid[i][columnIndex]);
            }
        }

        return output.toString();
    }

    static String decrypt(String cipherText, String key) {
        cipherText = normalizeText(cipherText);

        int columnCount = key.length();
        int rowCount = cipherText.length() / columnCount;

        char[][] grid = new char[rowCount][columnCount];
        int[] columnReadOrder = buildColumnReadOrder(key);

        int index = 0;
        for (int k = 0; k < columnReadOrder.length; k++) {
            int columnIndex = columnReadOrder[k];
            for (int i = 0; i < rowCount; i++) {
                grid[i][columnIndex] = cipherText.charAt(index++);
            }
        }

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                output.append(grid[i][j]);
            }
        }

        return output.toString();
    }

    static void printEncryptionGrid(String message, String key) {
        int columnCount = key.length();
        String paddedText = padWithX(normalizeText(message), columnCount);
        char[][] grid = buildGrid(paddedText, columnCount);

        System.out.println("Key         : " + key);
        System.out.print("Columns     : ");
        for (int i = 0; i < key.length(); i++) {
            System.out.print(key.charAt(i) + " ");
        }
        System.out.println();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        String plainText = "TIMEISMONEYTIMEISM";
        String key = "52413";

        String normalizedPlainText = normalizeText(plainText);
        String paddedPlainText = padWithX(normalizedPlainText, key.length());
        String cipherText = encrypt(plainText, key);
        String decryptedText = decrypt(cipherText, key);

        System.out.println("Plain text  : " + normalizedPlainText);
        System.out.println("Padded text : " + paddedPlainText);
        printEncryptionGrid(plainText, key);
        System.out.println("Cipher text : " + cipherText);
        System.out.println("Decrypted   : " + decryptedText);
    }
}