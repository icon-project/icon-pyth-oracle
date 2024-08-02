package icon.oracle.score.pyth;

import java.util.HexFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

public class PythTest extends TestBase {
    protected final ServiceManager sm = getServiceManager();
    protected final Account owner = sm.createAccount();
    protected Score pyth;


    static byte[][] guardians = new byte[][]{
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

    static byte[] emitter = HexFormat.of().parseHex("E101FAEDAC5851E32B9B23B5F9411A8C2BAC4AAE3ED4DD7B811DD1A72EA4AA71");

    @BeforeEach
    public void setup() throws Exception {
        pyth = sm.deploy(owner, Pyth.class, (Object)guardians, emitter);
    }

    @Test
    public void configureDistributions_onlyOwner() {
        String hex = "504e41550100000003b801000000040d001fc505f2ce9471d7dfc65695608c71ce588da4b44702c147f70f1accccbbcb0c40bdb0d39289af2c29f342f5b7399c70b32ece7ce31e1283cfb84c0a08de45d701027d9e0d338e08ffe7e07fef2648b257e8459db1ac5a38088a4d8796dc344e10424a4720ab20187bd9815b68bcabdd42c1c6a7ca4782e46fa2d5601d1983e3ddd701030d0d8ee4cc11eb23726333740cbbe3fa07a2c38b2a42c3ad141debb4ba09a0387e07732210b232bb1039af8eeae87b2890a4339accd50a3dd81db3b574918b4d0104d6fab54ebd40111b5879a2ac24db8ba11a8b0b92395c5cec63066438ee35600a16d8c7ef467089dac9f24619756556e7ba39c36f8c4a45d59a5ca4cae9896e2c01066dbd1c1ce8568537e539dc887e9032616d5f84aae52131dd1daa68c2e58c4f9b2131a939cf2aec81bec096c275a8722aacf8913cda2763d8d17388f4e3830d6d000845fe706d106f3b65ced514043a914f6a94a3722a3fc5b3272632907b1043c2d32ab6e7d489f7f6778e824ac94b96c4b30d740e365220ca076eeaddbf2b720d85010a41d49d089a2ef19432fe7c2fb189d33c8f41100d06b9f707a381ae19915d9c43367064c3f8520ece95dd61172aa8f944b09b9910c083262dab7d2fe74c05ba13000b3a4dc9467b1e3fabc35b587c262652ffb259bafd35fc0a6c1389af52b060374170d13ae2616a2bc15c38c24ec294588572541609ce6c0d60bf9f2b5366a06f49000c675d53a48d9ab9a64a3e774d6ad499eb2809f2fbc301c3ecd09de4f83cc12fd60c9485cc26c45b7ab14eda42b7a0373287d546ea60d6c0c25879811a8593913b000d1b6f095c3a6d5c45de09014f007efd72c28d292dcdb3780e9b6d9b3b3a9cca6e067f71ccb11d1e6ee33b6e992407bedd7dafc64e38bf3b5527b4eeecb26bfe3c000e692264eb763c689f4ba85a34682533fac1bff8e1d3a30da186f7e37b88d8f09b10904f953aeb2dc5be6868c70fd2fa8f66d0ef18a215e68ad4aba12afb51e2440110aeddc1d17efdb2cf7253174edd5e3ed3cf278427e0b4a4d6b66c30a79649da2d2d2900399b6abf3944f067dd189e6d62f1b2f224d8ffc07e9ca7cf5a4fd4a606011229de3b94a0a1e81880ca12603b4270daad878b13684850f5a4da0c3922542c2876e45f03e2bafd9754c1210df61afa2343245aa244eaab48769742ff2c186bf30066ac725600000000001ae101faedac5851e32b9b23b5f9411a8c2bac4aae3ed4dd7b811dd1a72ea4aa710000000004434adb01415557560000000000094deeab000027107a799736871ccacfe16c560d6defbea534da157d01005500e62df6c8b4a85fe1a67db44dc12de5db330f7ac66b72dc658afedf0f4a415b43000005d59d830f9a000000011ec55066fffffff80000000066ac72560000000066ac7256000005db9021a220000000011def02180b4a65ec5dad4267db652771307cfc4782e9f60fbaea3581630daf78bb82c3bea3dda7525b75d3af30635aa7a9b215189507a342dfb4d8df56339f3406732c59c6fbe29280ed18281e4cc4beef36b22a06fa504732044af6627bab48a2b8b1632bf4269da204ae7eab2375744233a57a53681876646bc2c7f55b28e609f0fba793866358f76172af03c3073935e2cb1647cccd54cbafb36a3ba78abe1b5906c19e1bc0a5051bc35f5cb8a3a642ebf687e2ae8c9887972dc46e9fa2e9bf5d4c73fff443e1b31f9707278f645b8d7ede14f2e751cef3e8e40b9fb9e447aa";
        byte[] data = HexFormat.of().parseHex(hex);
        // ParsedVAA.deserialize(data);
        pyth.invoke(owner, "parseUpdate", data);
    }


}
