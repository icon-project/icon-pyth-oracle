package icon.oracle.score.pyth.structs;

import score.Context;
import java.util.Arrays;

public class ParsedVAA {
    public static final int HEADER_LEN = 6;
    public static final int SIGNATURE_LEN = 66;

    public static final int GUARDIAN_SET_INDEX_POS = 1;
    public static final int LEN_SIGNER_POS = 5;

    public static final int VAA_NONCE_POS = 4;
    public static final int VAA_EMITTER_CHAIN_POS = 8;
    public static final int VAA_EMITTER_ADDRESS_POS = 10;
    public static final int VAA_SEQUENCE_POS = 42;
    public static final int VAA_CONSISTENCY_LEVEL_POS = 50;
    public static final int VAA_PAYLOAD_POS = 51;

    public static final int SIG_DATA_POS = 1;
    public static final int SIG_DATA_LEN = 64;
    public static final int SIG_RECOVERY_POS = SIG_DATA_POS + SIG_DATA_LEN;

    public byte version;
    public int guardianSetIndex;
    public int timestamp;
    public int nonce;
    public byte lenSigners;

    public short emitterChain;
    public byte[] emitterAddress;
    public long sequence;
    public byte consistencyLevel;
    public byte[] payload;

    public byte[] hash;

    // Constructor
    public ParsedVAA(byte version, int guardianSetIndex, int timestamp, int nonce, byte lenSigners,
            short emitterChain, byte[] emitterAddress, long sequence,
            byte consistencyLevel, byte[] payload, byte[] hash) {
        this.version = version;
        this.guardianSetIndex = guardianSetIndex;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.lenSigners = lenSigners;
        this.emitterChain = emitterChain;
        this.emitterAddress = emitterAddress;
        this.sequence = sequence;
        this.consistencyLevel = consistencyLevel;
        this.payload = payload;
        this.hash = hash;
    }

    public static ParsedVAA deserialize(byte[] data) throws IllegalArgumentException {
        // Extract version
        byte version = data[0];

        // Extract guardian_set_index (4 bytes)
        int guardianSetIndex = ((data[GUARDIAN_SET_INDEX_POS] & 0xFF) << 24) |
                ((data[GUARDIAN_SET_INDEX_POS + 1] & 0xFF) << 16) |
                ((data[GUARDIAN_SET_INDEX_POS + 2] & 0xFF) << 8) |
                (data[GUARDIAN_SET_INDEX_POS + 3] & 0xFF);

        // Extract len_signers
        byte lenSigners = data[LEN_SIGNER_POS];

        int bodyOffset = HEADER_LEN + SIGNATURE_LEN * lenSigners;

        // Validate body offset
        Context.require(bodyOffset < data.length, "Invalid VAA: body offset out of range");

        // Calculate hash of the body
        byte[] body = Arrays.copyOfRange(data, bodyOffset, data.length);
        byte[] hash = calculateKeccak256Hash(body);
        byte[] rehashed = calculateKeccak256Hash(hash);

        // Validate payload length
        Context.require(bodyOffset + VAA_PAYLOAD_POS <= data.length, "Invalid VAA: body offset exceeds data length");
        // Extract fields from body
        int timestamp = ((body[0] & 0xFF) << 24) |
                ((body[1] & 0xFF) << 16) |
                ((body[2] & 0xFF) << 8) |
                (body[3] & 0xFF);

        int nonce = ((body[VAA_NONCE_POS] & 0xFF) << 24) |
                ((body[VAA_NONCE_POS + 1] & 0xFF) << 16) |
                ((body[VAA_NONCE_POS + 2] & 0xFF) << 8) |
                (body[VAA_NONCE_POS + 3] & 0xFF);

        short emitterChain = (short) (((body[VAA_EMITTER_CHAIN_POS] & 0xFF) << 8) |
                (body[VAA_EMITTER_CHAIN_POS + 1] & 0xFF));

        byte[] emitterAddress = Arrays.copyOfRange(body, VAA_EMITTER_ADDRESS_POS, VAA_EMITTER_ADDRESS_POS + 32);
        long sequence = ((long) (body[VAA_SEQUENCE_POS] & 0xFF) << 56) |
                ((long) (body[VAA_SEQUENCE_POS + 1] & 0xFF) << 48) |
                ((long) (body[VAA_SEQUENCE_POS + 2] & 0xFF) << 40) |
                ((long) (body[VAA_SEQUENCE_POS + 3] & 0xFF) << 32) |
                ((long) (body[VAA_SEQUENCE_POS + 4] & 0xFF) << 24) |
                ((long) (body[VAA_SEQUENCE_POS + 5] & 0xFF) << 16) |
                ((long) (body[VAA_SEQUENCE_POS + 6] & 0xFF) << 8) |
                (body[VAA_SEQUENCE_POS + 7] & 0xFF);

        byte consistencyLevel = body[VAA_CONSISTENCY_LEVEL_POS];
        byte[] payload = Arrays.copyOfRange(body, VAA_PAYLOAD_POS, body.length);

        return new ParsedVAA(version, guardianSetIndex, timestamp, nonce, lenSigners,
                emitterChain, emitterAddress, sequence, consistencyLevel,
                payload, rehashed);
    }

    public static byte[] calculateKeccak256Hash(byte[] input) throws IllegalArgumentException {
        return Context.hash("keccak-256", input);
    }
}
