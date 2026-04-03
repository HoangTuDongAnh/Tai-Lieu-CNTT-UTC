public class MonoCipher {

    static String toUpperCaseText(String text) {
        return text.toUpperCase();
    }

    static boolean validateKey(String key) {
        key = toUpperCaseText(key);

        if (key.length() != 26) return false;

        boolean[] seen = new boolean[26];

        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);

            if (ch < 'A' || ch > 'Z') return false;

            int index = ch - 'A';
            if (seen[index]) return false;

            seen[index] = true;
        }

        return true;
    }

    static char[] buildEncryptionMap(String key) {
        key = toUpperCaseText(key);

        if (!validateKey(key)) {
            throw new IllegalArgumentException("Key khong hop le");
        }

        return key.toCharArray();
    }

    static char[] buildDecryptionMap(String key) {
        key = toUpperCaseText(key);

        if (!validateKey(key)) {
            throw new IllegalArgumentException("Key khong hop le");
        }

        char[] decryptionMap = new char[26];

        for (int i = 0; i < 26; i++) {
            char cipherChar = key.charAt(i);
            decryptionMap[cipherChar - 'A'] = (char) ('A' + i);
        }

        return decryptionMap;
    }

    static char encryptCharacter(char ch, char[] encryptionMap) {
        if (ch >= 'A' && ch <= 'Z') {
            return encryptionMap[ch - 'A'];
        }

        if (ch >= 'a' && ch <= 'z') {
            char upperChar = (char) (ch - 'a' + 'A');
            return Character.toLowerCase(encryptionMap[upperChar - 'A']);
        }

        return ch;
    }

    static char decryptCharacter(char ch, char[] decryptionMap) {
        if (ch >= 'A' && ch <= 'Z') {
            return decryptionMap[ch - 'A'];
        }

        if (ch >= 'a' && ch <= 'z') {
            char upperChar = (char) (ch - 'a' + 'A');
            return Character.toLowerCase(decryptionMap[upperChar - 'A']);
        }

        return ch;
    }

    static String encrypt(String text, String key) {
        char[] encryptionMap = buildEncryptionMap(key);
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            output.append(encryptCharacter(text.charAt(i), encryptionMap));
        }

        return output.toString();
    }

    static String decrypt(String text, String key) {
        char[] decryptionMap = buildDecryptionMap(key);
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            output.append(decryptCharacter(text.charAt(i), decryptionMap));
        }

        return output.toString();
    }

    public static void main(String[] args) {
        String plainText = "PENNYWISEPOUNDFO";
        String key = "KGOXPMUHCAYTJQWZRIVESFLDNB";

        String normalizedKey = toUpperCaseText(key);
        char[] encryptionMap = buildEncryptionMap(key);
        char[] decryptionMap = buildDecryptionMap(key);
        String cipherText = encrypt(plainText, key);
        String decryptedText = decrypt(cipherText, key);

        System.out.println("Plain text  : " + plainText);
        System.out.println("Key         : " + normalizedKey);
        System.out.println("Key valid   : " + validateKey(key));
        System.out.println("Cipher text : " + cipherText);
        System.out.println("Decrypted   : " + decryptedText);

        System.out.println("Encryption map:");
        for (int i = 0; i < 26; i++) {
            System.out.println((char) ('A' + i) + " -> " + encryptionMap[i]);
        }

        System.out.println("Decryption map:");
        for (int i = 0; i < 26; i++) {
            System.out.println((char) ('A' + i) + " -> " + decryptionMap[i]);
        }
    }
}