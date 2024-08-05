package icon.oracle.score.pyth.structs;

import java.util.Arrays;

import icon.oracle.score.pyth.utils.ByteUtil;
import score.Context;

public class Merkle {
    private static final byte[] MERKLE_LEAF_PREFIX = new byte[] { 0x00 };
    private static final byte[] MERKLE_NODE_PREFIX = new byte[] { 0x01 };

    public static int verifyMerkleProof(byte[] encodedProof, int proofOffset, byte[] root, byte[] leafData) {
        byte[] currentDigest = leafHash(leafData);
        int proofSize = ByteUtil.readU8(encodedProof, proofOffset);
        proofOffset += ByteUtil.U8_LENGTH;
        for (int i = 0; i < proofSize; i++) {
            byte[] siblingDigest = Arrays.copyOfRange(encodedProof, proofOffset, proofOffset + 20);
            proofOffset += 20;

            currentDigest = nodeHash(currentDigest, siblingDigest);
        }

        if (!Arrays.equals(currentDigest, root)) {
            return -1;
        }
        return proofOffset;
    }

    private static byte[] leafHash(byte[] data) {
        return hash(ByteUtil.encodePacked(MERKLE_LEAF_PREFIX, data));
    }

    private static byte[] hash(byte[] data) {
        return Arrays.copyOfRange(Context.hash("keccak-256", data), 0, 20);
    }

    public static byte[] nodeHash(byte[] childA, byte[] childB) {
        // Ensure childA is lexicographically smaller than childB
        if (compareBytes20(childA, childB)) {
            return hash(ByteUtil.encodePacked(MERKLE_NODE_PREFIX, childB, childA));
        }
        return hash(ByteUtil.encodePacked(MERKLE_NODE_PREFIX, childA, childB));
    }

    public static boolean compareBytes20(byte[] array1, byte[] array2) {
        for (int i = 0; i < 20; i++) {
            int val1 = Byte.toUnsignedInt(array1[i]);
            int val2 = Byte.toUnsignedInt(array2[i]);
            if (val1 > val2) {
                return true;
            } else if (val1 < val2) {
                return false;
            }
        }
        return false;
    }
}
