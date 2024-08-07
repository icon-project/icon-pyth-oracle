package icon.oracle.score.pyth;

import static icon.oracle.score.pyth.utils.ByteUtil.*;

import java.util.Arrays;

import icon.oracle.score.pyth.structs.ParsedVAA;
import icon.oracle.score.pyth.structs.Price;
import icon.oracle.score.pyth.utils.Constants;
import icon.oracle.score.pyth.utils.Errors;
import icon.oracle.score.pyth.utils.Merkle;
import score.Context;
import score.DictDB;

public class Accumulator {
    private static final int CURRENT_MAJOR_VERSION = 1;
    private static final int CURRENT_MINOR_VERSION = 0;

    public static final DictDB<byte[], byte[]> prices = Context.newDictDB("Pricesv3", byte[].class);

    public byte[] extractAndVerifyData(byte[] bytes) {
        // Ensure we have enough bytes to check the magic and versions
        Context.require(bytes.length > 6, Errors.InsufficientLength);

        byte[] magic = new byte[4];
        System.arraycopy(bytes, 0, magic, 0, 4);
        Context.require(Arrays.equals(magic, Constants.PYTHNET_ACCUMULATOR_UPDATE_MAGIC), "Invalid magic value.");

        int majorVersion = bytes[4];
        Context.require(majorVersion == CURRENT_MAJOR_VERSION, Errors.InvalidUpdateData);

        int minorVersion = bytes[5];
        Context.require(minorVersion >= CURRENT_MINOR_VERSION, Errors.InvalidUpdateData);

        int trailingHeaderSize = bytes[6];
        int updateType = bytes[trailingHeaderSize + 7];
        Context.require(updateType == Constants.WormholeMerkle, Errors.InvalidUpdateData);

        byte[] result = new byte[bytes.length - (trailingHeaderSize + 8)];
        System.arraycopy(bytes, trailingHeaderSize + 8, result, 0, result.length);
        return result;
    }

    public void parse(byte[] data) {
        data = extractAndVerifyData(data);
        int wormholeProofSize = readU16(data, 0);
        int offset = U16_LENGTH;

        byte[] encodedVaa = new byte[wormholeProofSize];
        System.arraycopy(data, offset, encodedVaa, 0, wormholeProofSize);
        offset += wormholeProofSize;

        ParsedVAA vaa = new Wormhole().parseAndVerifyVaa(encodedVaa);
        byte[] encodedPayload = vaa.payload;

        int payloadOffset = 0;

        byte[] magic = new byte[U32_LENGTH];
        System.arraycopy(encodedPayload, payloadOffset, magic, 0, U32_LENGTH);
        payloadOffset += U32_LENGTH;
        Context.require(Arrays.equals(magic, Constants.PYTHNET_ACCUMULATOR_UPDATE_WORMHOLE_VERIFICATION_MAGIC),
                Errors.InvalidUpdateData);

        int updateType = readU8(encodedPayload, payloadOffset);
        payloadOffset += U8_LENGTH;
        Context.require(updateType == Constants.WormholeMerkle, Errors.InvalidUpdateData);

        // Unused fields
        payloadOffset += U64_LENGTH;
        payloadOffset += U32_LENGTH;

        byte[] digest = new byte[20];
        System.arraycopy(encodedPayload, payloadOffset, digest, 0, 20);
        payloadOffset += 20;
        Context.require(payloadOffset <= encodedPayload.length, Errors.InvalidUpdateData);

        int numUpdates = data[offset];
        offset = offset + 1;

        parseAndVerifyPriceFeed(numUpdates, data, offset, digest);
    }

    public void parseAndVerifyPriceFeed(int numUpdates, byte[] data, int offset, byte[] digest) {
        Merkle merkle = new Merkle();
        for (int i = 0; i < numUpdates; i++) {

            int messageSize = readU16(data, offset);
            offset += U16_LENGTH;

            byte[] encodedMessage = new byte[messageSize];
            System.arraycopy(data, offset, encodedMessage, 0, messageSize);
            offset += messageSize;

            offset = merkle.verifyMerkleProof(data, offset, digest, encodedMessage);
            Context.require(offset > 0, Errors.InvalidUpdateData);

            parsePrice(encodedMessage);
        }
    }

    // Pre allocate for gas optimization
    private byte[] priceId = new byte[32];
    private byte[] price = new byte[28];
    public void parsePrice(byte[] encodedMessage) {
        int messageType = readU8(encodedMessage, 0);
        Context.require(messageType == Constants.PriceFeed, Errors.InvalidUpdateData);

        System.arraycopy(encodedMessage, 1, priceId, 0, 32);
        System.arraycopy(encodedMessage, 33, price, 0, 28);
        long publishTime = readU64(price, Price.TIME_POS);

        Context.require(85 <= encodedMessage.length, Errors.InvalidUpdateData);
        byte[] prevPrice = prices.get(priceId);
        if (prevPrice == null || readU64(prevPrice, Price.TIME_POS) < publishTime) {
            prices.set(priceId, price);
        }

    }

    public static Price getPrice(byte[] id ) {
        return new Price(prices.get(id));
    }


}
