public class VigenereRepeat {

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

    static String buildRepeatedKey(String key, int length) {
        key = normalizeText(key);
        StringBuilder repeatedKey = new StringBuilder();

        for (int i = 0; i < length; i++) {
            repeatedKey.append(key.charAt(i % key.length()));
        }

        return repeatedKey.toString();
    }

    static String encrypt(String message, String key) {
        message = normalizeText(message);
        String repeatedKey = buildRepeatedKey(key, message.length());

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            int plainValue = charToValue(message.charAt(i));
            int keyValue = charToValue(repeatedKey.charAt(i));
            output.append(valueToChar(plainValue + keyValue));
        }

        return output.toString();
    }

    static String decrypt(String cipherText, String key) {
        cipherText = normalizeText(cipherText);
        String repeatedKey = buildRepeatedKey(key, cipherText.length());

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < cipherText.length(); i++) {
            int cipherValue = charToValue(cipherText.charAt(i));
            int keyValue = charToValue(repeatedKey.charAt(i));
            output.append(valueToChar(cipherValue - keyValue));
        }

        return output.toString();
    }

    public static void main(String[] args) {
        String plainText = "WHENINROMEDO";
        String key = "THETRU";

        String normalizedPlainText = normalizeText(plainText);
        String normalizedKey = normalizeText(key);
        String repeatedKey = buildRepeatedKey(key, normalizedPlainText.length());
        String cipherText = encrypt(plainText, key);
        String decryptedText = decrypt(cipherText, key);

        System.out.println("Plain text  : " + normalizedPlainText);
        System.out.println("Key         : " + normalizedKey);
        System.out.println("Repeated key: " + repeatedKey);
        System.out.println("Cipher text : " + cipherText);
        System.out.println("Decrypted   : " + decryptedText);
    }
}