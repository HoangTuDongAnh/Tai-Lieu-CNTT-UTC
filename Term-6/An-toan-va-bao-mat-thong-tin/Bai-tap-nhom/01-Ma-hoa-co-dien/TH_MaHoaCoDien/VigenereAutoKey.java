public class VigenereAutoKey {

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

    static int charToValue(char ch) {
        return ch - 'A';
    }

    static char valueToChar(int value) {
        return (char) ('A' + (value % 26 + 26) % 26);
    }

    static String buildAutoKey(String key, String message) {
        key = normalizeText(key);
        message = normalizeText(message);

        StringBuilder fullKey = new StringBuilder(key);

        int i = 0;
        while (fullKey.length() < message.length()) {
            fullKey.append(message.charAt(i));
            i++;
        }

        return fullKey.toString();
    }

    static String encrypt(String message, String key) {
        message = normalizeText(message);
        String fullKey = buildAutoKey(key, message);

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            int plainValue = charToValue(message.charAt(i));
            int keyValue = charToValue(fullKey.charAt(i));
            output.append(valueToChar(plainValue + keyValue));
        }

        return output.toString();
    }

    static String decrypt(String cipherText, String key) {
        cipherText = normalizeText(cipherText);
        key = normalizeText(key);

        StringBuilder plainText = new StringBuilder();
        StringBuilder currentKey = new StringBuilder(key);

        for (int i = 0; i < cipherText.length(); i++) {
            int cipherValue = charToValue(cipherText.charAt(i));
            int keyValue = charToValue(currentKey.charAt(i));

            char plainChar = valueToChar(cipherValue - keyValue);
            plainText.append(plainChar);
            currentKey.append(plainChar);
        }

        return plainText.toString();
    }

    public static void main(String[] args) {
        String plainText = "BARKINGDOGSS";
        String key = "LIKEFA";

        String normalizedPlainText = normalizeText(plainText);
        String normalizedKey = normalizeText(key);
        String fullKey = buildAutoKey(key, plainText);
        String cipherText = encrypt(plainText, key);
        String decryptedText = decrypt(cipherText, key);

        System.out.println("Plain text  : " + normalizedPlainText);
        System.out.println("Initial key : " + normalizedKey);
        System.out.println("Full key    : " + fullKey);
        System.out.println("Cipher text : " + cipherText);
        System.out.println("Decrypted   : " + decryptedText);
    }
}