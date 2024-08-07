package icon.oracle.score.pyth.utils;

import java.util.Arrays;

import score.Context;

public class Merkle {
    private static final byte MERKLE_LEAF_PREFIX = 0x00;
    private static final byte MERKLE_NODE_PREFIX = 0x01;

    // pre allocate for gas optimization
    private byte[] currentDigest = new byte[20];
    private byte[] node = new byte[41];

    public Merkle() {
        node[0] = MERKLE_NODE_PREFIX;
    }

    public int verifyMerkleProof(byte[] encodedProof, int proofOffset, byte[] root, byte[] leafData) {
        leafHash(leafData, currentDigest);
        int proofSize = ByteUtil.readU8(encodedProof, proofOffset);
        proofOffset += ByteUtil.U8_LENGTH;
        for (int i = 0; i < proofSize; i++) {
            if (compareNode(currentDigest, encodedProof, proofOffset)) {
                System.arraycopy(encodedProof, proofOffset, node, 1, 20);
                System.arraycopy(currentDigest, 0, node, 21, 20);
            } else {
                System.arraycopy(encodedProof, proofOffset, node, 21, 20);
                System.arraycopy(currentDigest, 0, node, 1, 20);
            }

            hash20(node,currentDigest);
            proofOffset += 20;

        }
        if (!Arrays.equals(currentDigest, root)) {
            return -1;
        }
        return proofOffset;
    }

    private void leafHash(byte[] data, byte[] dst) {
        hash20(ByteUtil.encodePacked(new byte[]{MERKLE_LEAF_PREFIX}, data), dst);
    }

    private void hash20(byte[] data,  byte[] dst) {
        System.arraycopy(Context.hash("keccak-256", data), 0, dst, 0, 20);
    }

    public static boolean compareNode(byte[] digest, byte[] proof, int offset) {
        for (int i = 0; i < 20; i++) {
            int a = Byte.toUnsignedInt(digest[ i]);
            int b = Byte.toUnsignedInt(proof[offset + i]);
            if (a != b) {
                return a > b;
            }
        }
        return false;
    }
}
