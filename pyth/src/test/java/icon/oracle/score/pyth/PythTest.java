package icon.oracle.score.pyth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HexFormat;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

import icon.oracle.score.pyth.structs.Price;
import icon.oracle.score.pyth.utils.Checks;
import icon.oracle.score.pyth.utils.Errors;

public class PythTest extends TestBase {
    protected final ServiceManager sm = getServiceManager();
    protected final Account owner = sm.createAccount();
    protected Score pyth;

    byte[][] guardians = new byte[][] {
            HexFormat.of().parseHex("5893B5A76c3f739645648885bDCcC06cd70a3Cd3"),
            HexFormat.of().parseHex("fF6CB952589BDE862c25Ef4392132fb9D4A42157"),
            HexFormat.of().parseHex("114De8460193bdf3A2fCf81f86a09765F4762fD1"),
            HexFormat.of().parseHex("107A0086b32d7A0977926A205131d8731D39cbEB"),
            HexFormat.of().parseHex("8C82B2fd82FaeD2711d59AF0F2499D16e726f6b2"),
            HexFormat.of().parseHex("11b39756C042441BE6D8650b69b54EbE715E2343"),
            HexFormat.of().parseHex("54Ce5B4D348fb74B958e8966e2ec3dBd4958a7cd"),
            HexFormat.of().parseHex("15e7cAF07C4e3DC8e7C469f92C8Cd88FB8005a20"),
            HexFormat.of().parseHex("74a3bf913953D695260D88BC1aA25A4eeE363ef0"),
            HexFormat.of().parseHex("000aC0076727b35FBea2dAc28fEE5cCB0fEA768e"),
            HexFormat.of().parseHex("AF45Ced136b9D9e24903464AE889F5C8a723FC14"),
            HexFormat.of().parseHex("f93124b7c738843CBB89E864c862c38cddCccF95"),
            HexFormat.of().parseHex("D2CC37A4dc036a8D232b48f62cDD4731412f4890"),
            HexFormat.of().parseHex("DA798F6896A3331F64b48c12D1D57Fd9cbe70811"),
            HexFormat.of().parseHex("71AA1BE1D36CaFE3867910F99C09e347899C19C3"),
            HexFormat.of().parseHex("8192b6E7387CCd768277c17DAb1b7a5027c0b3Cf"),
            HexFormat.of().parseHex("178e21ad2E77AE06711549CFBB1f9c7a9d8096e8"),
            HexFormat.of().parseHex("5E1487F35515d02A92753504a8D75471b9f49EdB"),
            HexFormat.of().parseHex("6FbEBc898F403E4773E95feB15E80C9A99c8348d")
    };

    byte[] emitter = HexFormat.of().parseHex("E101FAEDAC5851E32B9B23B5F9411A8C2BAC4AAE3ED4DD7B811DD1A72EA4AA71");
    List<PriceFeed> priceFeeds;

    @BeforeEach
    public void setup() throws Exception {
        pyth = sm.deploy(owner, Pyth.class, guardians, emitter);
        ObjectMapper objectMapper = new ObjectMapper();
        Path path = Paths.get("src/test/java/icon/oracle/score/pyth/data/data.json");
        priceFeeds = objectMapper.readValue(
                new File(path.toString()),
                new TypeReference<List<PriceFeed>>() {
                });
    }

    @Test
    public void updatePriceFeed() {
        // Arrange
        PriceFeed feed = priceFeeds.get(0);
        byte[][] hex = feed.getBinary().getBytes();

        // Act
        pyth.invoke(owner, "updatePriceFeed", (Object) hex);

        // Assert
        for (ParsedData parsed : feed.getParsed()) {
            Price price = (Price) pyth.call("getPrice", HexFormat.of().parseHex(parsed.getId()));
            assertEquals(price.price, parsed.getPrice().getPrice());
            assertEquals(price.conf, parsed.getPrice().getConf());
            assertEquals(price.expo, parsed.getPrice().getExpo());
            assertEquals(price.publishTime, parsed.getPrice().getPublishTime());
        }
    }

    @Test
    public void updatePriceFeed_multiple() {
        // Arrange
        PriceFeed endFeed = priceFeeds.get(2);

        // Act
        pyth.invoke(owner, "updatePriceFeed", (Object) priceFeeds.get(0).getBinary().getBytes());
        pyth.invoke(owner, "updatePriceFeed", (Object) priceFeeds.get(1).getBinary().getBytes());
        pyth.invoke(owner, "updatePriceFeed", (Object) priceFeeds.get(2).getBinary().getBytes());

        // Assert
        for (ParsedData parsed : endFeed.getParsed()) {
            Price price = (Price) pyth.call("getPrice", HexFormat.of().parseHex(parsed.getId()));
            assertEquals(price.price, parsed.getPrice().getPrice());
            assertEquals(price.conf, parsed.getPrice().getConf());
            assertEquals(price.expo, parsed.getPrice().getExpo());
            assertEquals(price.publishTime, parsed.getPrice().getPublishTime());
        }
    }

    @Test
    public void updatePriceFeed_outOfOrder() {
        // Arrange
        PriceFeed endFeed = priceFeeds.get(2);

        // Act
        pyth.invoke(owner, "updatePriceFeed", (Object) priceFeeds.get(2).getBinary().getBytes());
        pyth.invoke(owner, "updatePriceFeed", (Object) priceFeeds.get(0).getBinary().getBytes());

        // Assert
        for (ParsedData parsed : endFeed.getParsed()) {
            Price price = (Price) pyth.call("getPrice", HexFormat.of().parseHex(parsed.getId()));
            assertEquals(price.price, parsed.getPrice().getPrice());
            assertEquals(price.conf, parsed.getPrice().getConf());
            assertEquals(price.expo, parsed.getPrice().getExpo());
            assertEquals(price.publishTime, parsed.getPrice().getPublishTime());
        }
    }

    @Test
    public void wormhole_NoQuorum() {
        // Arrange
        byte[][] extendedGuardians = new byte[guardians.length * 2][];
        System.arraycopy(guardians, 0, extendedGuardians, 0, guardians.length);
        System.arraycopy(guardians, 0, extendedGuardians, guardians.length, guardians.length);

        pyth.invoke(owner, "setGuardians", (Object) extendedGuardians);

        PriceFeed feed = priceFeeds.get(0);
        byte[][] hex = feed.getBinary().getBytes();

        // Act & Assert
        Executable noQuorum = () -> pyth.invoke(owner, "updatePriceFeed", (Object) hex);
        expectErrorMessage(noQuorum, Errors.NoQuorum);
    }

    @Test
    public void wormhole_InvalidSignatures() {
        // Arrange
        guardians[2][5] = 0x52;
        pyth.invoke(owner, "setGuardians", (Object) guardians);

        PriceFeed feed = priceFeeds.get(0);
        byte[][] hex = feed.getBinary().getBytes();

        // Act & Assert
        Executable guardianSignatureError = () -> pyth.invoke(owner, "updatePriceFeed", (Object) hex);
        expectErrorMessage(guardianSignatureError, Errors.GuardianSignatureError);
    }

    @Test
    public void wormhole_InvalidEmitter() {
        // Arrange
        emitter[2] = 0x33;
        pyth.invoke(owner, "setEmitter", (Object) emitter);

        PriceFeed feed = priceFeeds.get(0);
        byte[][] hex = feed.getBinary().getBytes();

        // Act & Assert
        Executable invalidEmitter = () -> pyth.invoke(owner, "updatePriceFeed", (Object) hex);
        expectErrorMessage(invalidEmitter, Errors.InvalidEmitter);
    }


    @Test
    public void permissions() {
        Account user = sm.createAccount();
        Executable onlyOwner = () -> pyth.invoke(user, "setEmitter", (Object) emitter);
        expectErrorMessage(onlyOwner, Checks.Errors.ONLY_OWNER);

        onlyOwner = () ->  pyth.invoke(user, "setGuardians", (Object) guardians);
        expectErrorMessage(onlyOwner, Checks.Errors.ONLY_OWNER);

    }

    public static void expectErrorMessage(Executable contractCall, String expectedErrorMessage) {
        Exception e = Assertions.assertThrows(Exception.class, contractCall);
        assertTrue(e.getMessage().contains(expectedErrorMessage));
    }

}
