public class CeasarCipher
{
    public static String encrypt (String plainText, int key) 
    {
        StringBuilder res = new StringBuilder();
        key = key % 26;
        
        for (char ch : plainText.toCharArray()) {
            char enChar = (char) ((ch - 'A' + key) % 26 + 'A');
            res.append(enChar);  
        }
        return res.toString();
    }
    
    public static void main(String[] args) {
        String plainText = "SAVEFORARAINYDAY";
        int key = 25;
        
        String encrypted = encrypt(plainText, key);
        System.out.println("Ban ma: " + encrypted);
    }
}
