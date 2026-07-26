public class Bai1_DES
{
    public static void main(String[] args)
    {
        String plainHex = "02468aceeca86420";
        String keyHex = "0f1571c947d9e859";

        DESAlgorithm des = new DESAlgorithm();
        DESUtil util = new DESUtil();

        String plainBin = util.hexToBin(plainHex);
        String keyBin = util.hexToBin(keyHex);

        String cipherBin = des.encryptBlockBinary(plainBin, keyBin);
        String cipherHex = util.binToHex(cipherBin);

        String[] subKeys = des.getSubKeysFromHex(keyHex);

        System.out.println("===== THUC HANH DES =====");
        System.out.println("Plaintext (hex): " + plainHex);
        System.out.println("Key       (hex): " + keyHex);
        System.out.println();

        System.out.println("Plaintext (bin): " + plainBin);
        System.out.println("Key       (bin): " + keyBin);
        System.out.println();

        System.out.println("16 khoa con:");
        for (int i = 0; i < subKeys.length; i++) {
            System.out.println("K" + (i + 1) + " = " + subKeys[i]);
        }

        System.out.println();
        System.out.println("Ciphertext (bin): " + cipherBin);
        System.out.println("Ciphertext (hex): " + cipherHex);
        System.out.println();
        System.out.println("Ket qua ky vong : DA02CE3A89ECAC3B");
        
        des.printDecryptRoundInfo(cipherHex, keyHex, 3);
    }
}