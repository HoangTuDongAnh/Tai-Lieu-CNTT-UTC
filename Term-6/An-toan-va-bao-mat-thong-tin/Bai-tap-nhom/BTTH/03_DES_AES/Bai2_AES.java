public class Bai2_AES
{
    public static void main(String[] args)
    {
        String plainHex = "4AEB5D62EC3B55DBF5D5A87708E2FF1E";
        String keyHex = "6704c20e086b3f537ae5721f486dc559";

        AESAlgorithm aes = new AESAlgorithm();
        AESUtil util = new AESUtil();

        String cipherHex = aes.encryptHex(plainHex, keyHex);
        int[] expanded = aes.expandKeyHex(keyHex);

        System.out.println("===== THUC HANH AES-128 =====");
        System.out.println("Input  (hex): " + plainHex);
        System.out.println("Key    (hex): " + keyHex);
        System.out.println("Output (hex): " + cipherHex);
        System.out.println("Ky vong      : FF0B844A0853BF7C6934AB4364148FB9");
        System.out.println();

        System.out.println("11 khoa vong:");
        for (int round = 0; round <= 10; round++) {
            int[][] rk = aes.getRoundKeyStateFromExpanded(expanded, round);
            int[] rkBytes = util.stateToBytes(rk);
            System.out.println("RoundKey " + round + ": " + util.bytesToHex(rkBytes));
        }
    }
}