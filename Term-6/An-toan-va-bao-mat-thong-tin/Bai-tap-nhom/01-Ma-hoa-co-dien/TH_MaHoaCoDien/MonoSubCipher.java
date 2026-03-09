public class MonoSubCipher
{
    public static String encrypt(String plain, String cipher, String plainText) {
        StringBuilder res = new StringBuilder();
        
        for (char ch : plainText.toCharArray()) {
            int index = plain.indexOf(ch);
            
            res.append(cipher.charAt(index));
        }
        
        return res.toString();
    }
    
    public static void main(String[] args) {
        String plain = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String cipher = "KGOXPMUHCAYTJQWZRIVESFLDNB";
        String plainText = "PENNYWISEPOUNDFO";
        
        String encrypted = encrypt(plain, cipher, plainText);
        System.out.println("Ban ma: " + encrypted);
    }
}
