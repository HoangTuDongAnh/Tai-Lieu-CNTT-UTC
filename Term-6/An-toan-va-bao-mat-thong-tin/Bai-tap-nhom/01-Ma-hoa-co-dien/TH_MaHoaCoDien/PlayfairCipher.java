public class PlayfairCipher
{
    public static char[][] genMatKey(String key) {
        key = key.replace('J', 'I');
        StringBuilder sb = new StringBuilder();
        boolean[] used = new boolean[26];
        
        // J gop voi I
        used['J' - 'A'] = true;
        
        // Them cac ky tu cua key truoc
        for (char c : key.toCharArray()) {
            if (!used[c - 'A']) {
                used[c - 'A'] = true;
                sb.append(c);
            }
        }
        
        // Them cac ky tu con lai
        for (char c = 'A'; c <= 'Z'; c++) {
            if (!used[c - 'A']) {
                used[c - 'A'] = true;
                sb.append(c);
            }
        }
        
        char[][] matKey = new char[5][5];
        int index = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                matKey[i][j] = sb.charAt(index++);
            }
        }
        
        return matKey;
    }
    
    public static String prepText(String text) {
        text = text.replace('J', 'I');
        StringBuilder res = new StringBuilder();
        int i = 0;
        
        while (i < text.length()) { 
            char first = text.charAt(i);

            if (i == text.length() - 1) {
                res.append(first).append('X');
                i++;
            } else {
                char second = text.charAt(i + 1);

                if (first == second) {
                    res.append(first).append('X');
                    i++; 
                } else {
                    res.append(first).append(second);
                    i += 2;
                }
            }
        }
        
        return res.toString();
    }
    
    public static int[] findPos(char[][] matKey, char ch) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (matKey[i][j] == ch) {
                    return new int[]{i, j};
                }
            }
        }
        
        return null;
    }
    
    public static String encrypt(String plainText, String key) {
        char[][] matKey = genMatKey(key);
        String prep = prepText(plainText);
        StringBuilder res = new StringBuilder();
        
        for (int i = 0; i < prep.length(); i += 2) {
            char a = prep.charAt(i);
            char b = prep.charAt(i+1);
            
            int[] posA = findPos(matKey, a);
            int[] posB = findPos(matKey, b);
            
            int rowA = posA[0], colA = posA[1];
            int rowB = posB[0], colB = posB[1];
            
            if (rowA == rowB) {
                res.append(matKey[rowA][(colA + 1) % 5]);
                res.append(matKey[rowB][(colB + 1) % 5]);
            }
            
            else if (colA == colB) {
                res.append(matKey[(rowA + 1) % 5][colA]);
                res.append(matKey[(rowB + 1) % 5][colB]);
            }
            
            else {
                res.append(matKey[rowA][colB]);
                res.append(matKey[rowB][colA]);
            }
        }
        
        return res.toString();
    }
    
    public static void printMatrix(char[][] matrix) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args) {
        String key = "SAVEFORA";
        String plaintext = "STILLWATERSR";

        char[][] matrix = genMatKey(key);

        System.out.println("Ma tran khoa:");
        printMatrix(matrix);

        System.out.println("Plaintext goc     : " + plaintext);
        System.out.println("Plaintext xu ly   : " + prepText(plaintext));
        System.out.println("Ciphertext        : " + encrypt(plaintext, key));
    }
}
