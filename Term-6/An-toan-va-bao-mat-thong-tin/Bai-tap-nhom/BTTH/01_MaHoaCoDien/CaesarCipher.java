public class CaesarCipher {

    static int normalizeKey(int key) {
        return (key % 26 + 26) % 26;
    }

    static char shiftCharacter(char ch, int key) {
        key = normalizeKey(key);

        if (ch >= 'A' && ch <= 'Z') {
            return (char) ('A' + (ch - 'A' + key) % 26);
        }

        if (ch >= 'a' && ch <= 'z') {
            return (char) ('a' + (ch - 'a' + key) % 26);
        }

        return ch;
    }

    static String encrypt(String text, int key) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            output.append(shiftCharacter(text.charAt(i), key));
        }

        return output.toString();
    }

    static String decrypt(String text, int key) {
        return encrypt(text, -key);
    }

    static void bruteForceDecrypt(String cipherText) {
        for (int key = 0; key < 26; key++) {
            String possiblePlainText = decrypt(cipherText, key);
            System.out.println("Key = " + key + " -> Plain text = " + possiblePlainText);
        }
    }

    public static void main(String[] args) {
        String plainText = "SAVEFORARAINYDAY";
        int key = 25;

        String cipherText = encrypt(plainText, key);
        String decryptedText = decrypt(cipherText, key);

        System.out.println("Plain text : " + plainText);
        System.out.println("Key        : " + normalizeKey(key));
        System.out.println("Cipher text: " + cipherText);
        System.out.println("Decrypted  : " + decryptedText);

        System.out.println("Brute force decrypt:");
        bruteForceDecrypt(cipherText);
    }
}