package icon.oracle.score.pyth.structs;

import static icon.oracle.score.pyth.utils.ByteUtil.*;

public class Price {
    public static final int PRICE_POS = 0;
    public static final int CONF_POS = 8;
    public static final int EXPO_POS = 16;
    public static final int TIME_POS = 20;
    public long price;
    public long conf;
    public long expo;
    public long publishTime;

    public Price(byte[] encoded) {
        price = readU64(encoded, PRICE_POS);
        conf = readU64(encoded, CONF_POS);
        expo = readI32(encoded, EXPO_POS);
        publishTime = readU64(encoded, TIME_POS);
    }
}