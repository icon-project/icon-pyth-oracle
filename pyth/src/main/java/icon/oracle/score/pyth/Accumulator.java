package icon.oracle.score.pyth;

import static icon.oracle.score.pyth.utils.ByteUtil.*;

import java.util.Arrays;

import icon.oracle.score.pyth.structs.Merkle;
import icon.oracle.score.pyth.structs.ParsedVAA;
import icon.oracle.score.pyth.structs.Price;
import icon.oracle.score.pyth.utils.Constants;
import icon.oracle.score.pyth.utils.Errors;
import score.Context;

public class Accumulator {
    private static final int CURRENT_MAJOR_VERSION = 1;
    private static final int CURRENT_MINOR_VERSION = 0;

    public static byte[] extractAndVerifyData(byte[] bytes) {
        // Ensure we have enough bytes to check the magic and versions
        // 4 bytes for magic + 1 for majorVersion + 1 for minorVersion
        Context.require(bytes.length > 6, Errors.Accumulator_InsufficientLength);

        byte[] magic = Arrays.copyOfRange(bytes, 0, 4);
        Context.require(Arrays.equals(magic, Constants.PYTHNET_ACCUMULATOR_UPDATE_MAGIC), "Invalid magic value.");

        int majorVersion = bytes[4];
        Context.require(majorVersion == CURRENT_MAJOR_VERSION);

        int minorVersion = bytes[5];
        Context.require(minorVersion >= CURRENT_MINOR_VERSION);

        int trailingHeaderSize = bytes[6];
        int updateType = bytes[trailingHeaderSize + 7];
        Context.require(updateType == Constants.WormholeMerkle);

        return Arrays.copyOfRange(bytes, trailingHeaderSize + 8, bytes.length);
    }

    public static void parse(byte[] data) {
        data = Accumulator.extractAndVerifyData(data);
        int wormholeProofSize = readU16(data, 0);
        int offset = U16_LENGTH;
        byte[] encodedVaa = Arrays.copyOfRange(data, offset, offset + wormholeProofSize);
        offset += wormholeProofSize;
        ParsedVAA vaa = Wormhole.parseAndVerifyVaa(encodedVaa);

        byte[] encodedPayload = vaa.payload;

        int payloadOffset = 0;
        byte[] magic = Arrays.copyOfRange(encodedPayload, payloadOffset, payloadOffset + U32_LENGTH);
        payloadOffset += U32_LENGTH;
        Context.require(Arrays.equals(magic, Constants.PYTHNET_ACCUMULATOR_UPDATE_WORMHOLE_VERIFICATION_MAGIC),
                Errors.InvalidUpdateData);

        int updateType = readU8(encodedPayload, payloadOffset);
        payloadOffset += U8_LENGTH;
        Context.require(updateType == Constants.WormholeMerkle, Errors.InvalidUpdateData);

        // Unused fields
        payloadOffset += U64_LENGTH;
        payloadOffset += U32_LENGTH;

        byte[] digest = Arrays.copyOfRange(encodedPayload, payloadOffset, payloadOffset + 20);
        payloadOffset += 20;
        Context.require(payloadOffset <= encodedPayload.length, Errors.InvalidUpdateData);

        int numUpdates = data[offset];
        offset = offset + 1;

        parseAndVerifyPriceFeed(numUpdates, data, offset, digest);
    }

    public static void parseAndVerifyPriceFeed(int numUpdates, byte[] data, int offset, byte[] digest) {
        for (int i = 0; i < numUpdates; i++) {

            int messageSize = readU16(data, offset);
            offset += U16_LENGTH;
            byte[] encodedMessage = Arrays.copyOfRange(data, offset, offset + messageSize);
            offset += messageSize;

            offset = Merkle.verifyMerkleProof(data, offset, digest, encodedMessage);
            Context.require(offset > 0, Errors.InvalidUpdateData);

            int messageType = readU8(encodedMessage, 0);
            Context.require(messageType == Constants.PriceFeed, Errors.InvalidUpdateData);
            parsePrice(encodedMessage);
        }
    }

    public static void parsePrice(byte[] encodedMessage) {
        int priceOffset = 1;
        byte[] priceId = Arrays.copyOfRange(encodedMessage, priceOffset, priceOffset + 32);
        priceOffset += 32;
        Price price = new Price();

        price.price = readU64(encodedMessage, priceOffset);
        priceOffset += U64_LENGTH;

        price.conf = readU64(encodedMessage, priceOffset);
        priceOffset += U64_LENGTH;

        price.expo = readI32(encodedMessage, priceOffset);
        priceOffset += U32_LENGTH;

        price.publishTime = readU64(encodedMessage, priceOffset);
        priceOffset += U64_LENGTH;

        // Ignore prev publish time
        priceOffset += U64_LENGTH;

        price.emaPrice = readU64(encodedMessage, priceOffset);
        priceOffset += U64_LENGTH;

        price.emaConf = readU64(encodedMessage, priceOffset);
        priceOffset += U64_LENGTH;

        Context.require(priceOffset <= encodedMessage.length);
        System.out.println(bytesToHex(priceId));
        System.out.println(price.price);
        System.out.println(price.conf);
        System.out.println(price.expo);
        System.out.println(price.publishTime);
        System.out.println(price.emaPrice);
        System.out.println(price.emaConf);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
