public class PlayfairCipher {

    static char[][] keyMatrix = new char[5][5];
    static int[] rowPositions = new int[26];
    static int[] colPositions = new int[26];

    static String normalizeText(String text) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = Character.toUpperCase(text.charAt(i));

            if (ch >= 'A' && ch <= 'Z') {
                if (ch == 'J') ch = 'I';
                output.append(ch);
            }
        }

        return output.toString();
    }

    static void buildKeyMatrix(String key) {
        boolean[] used = new boolean[26];
        used['J' - 'A'] = true;

        String source = normalizeText(key) + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        int index = 0;
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            int alphabetIndex = ch - 'A';

            if (!used[alphabetIndex]) {
                used[alphabetIndex] = true;
                keyMatrix[index / 5][index % 5] = ch;
                rowPositions[alphabetIndex] = index / 5;
                colPositions[alphabetIndex] = index % 5;
                index++;
            }
        }

        rowPositions['J' - 'A'] = rowPositions['I' - 'A'];
        colPositions['J' - 'A'] = colPositions['I' - 'A'];
    }

    static String splitIntoDigraphs(String message) {
        message = normalizeText(message);
        StringBuilder output = new StringBuilder();

        int i = 0;
        while (i < message.length()) {
            char first = message.charAt(i);
            char second;

            if (i + 1 >= message.length()) {
                second = 'X';
                i += 1;
            } else if (message.charAt(i) == message.charAt(i + 1)) {
                second = 'X';
                i += 1;
            } else {
                second = message.charAt(i + 1);
                i += 2;
            }

            output.append(first).append(second);
        }

        return output.toString();
    }

    static String encryptPair(char first, char second) {
        int r1 = rowPositions[first - 'A'];
        int c1 = colPositions[first - 'A'];
        int r2 = rowPositions[second - 'A'];
        int c2 = colPositions[second - 'A'];

        if (r1 == r2) {
            return "" + keyMatrix[r1][(c1 + 1) % 5] + keyMatrix[r2][(c2 + 1) % 5];
        }

        if (c1 == c2) {
            return "" + keyMatrix[(r1 + 1) % 5][c1] + keyMatrix[(r2 + 1) % 5][c2];
        }

        return "" + keyMatrix[r1][c2] + keyMatrix[r2][c1];
    }

    static String decryptPair(char first, char second) {
        int r1 = rowPositions[first - 'A'];
        int c1 = colPositions[first - 'A'];
        int r2 = rowPositions[second - 'A'];
        int c2 = colPositions[second - 'A'];

        if (r1 == r2) {
            return "" + keyMatrix[r1][(c1 + 4) % 5] + keyMatrix[r2][(c2 + 4) % 5];
        }

        if (c1 == c2) {
            return "" + keyMatrix[(r1 + 4) % 5][c1] + keyMatrix[(r2 + 4) % 5][c2];
        }

        return "" + keyMatrix[r1][c2] + keyMatrix[r2][c1];
    }

    static String encrypt(String message) {
        String preparedText = splitIntoDigraphs(message);
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < preparedText.length(); i += 2) {
            output.append(encryptPair(preparedText.charAt(i), preparedText.charAt(i + 1)));
        }

        return output.toString();
    }

    static String decrypt(String cipherText) {
        cipherText = normalizeText(cipherText);
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < cipherText.length(); i += 2) {
            output.append(decryptPair(cipherText.charAt(i), cipherText.charAt(i + 1)));
        }

        return output.toString();
    }

    static void printKeyMatrix() {
        System.out.println("Key matrix:");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.print(keyMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        String plainText = "STILLWATERSR";
        String key = "SAVEFORA";

        buildKeyMatrix(key);

        String preparedText = splitIntoDigraphs(plainText);
        String cipherText = encrypt(plainText);
        String decryptedText = decrypt(cipherText);

        System.out.println("Plain text   : " + normalizeText(plainText));
        System.out.println("Key          : " + normalizeText(key));
        printKeyMatrix();
        System.out.println("Prepared text: " + preparedText);
        System.out.println("Cipher text  : " + cipherText);
        System.out.println("Decrypted    : " + decryptedText);
    }
}