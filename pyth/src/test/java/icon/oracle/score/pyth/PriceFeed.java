package icon.oracle.score.pyth;

import java.util.HexFormat;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PriceFeed {
      private Binary binary;
    private List<ParsedData> parsed;

    public Binary getBinary() {
        return binary;
    }

    public void setBinary(Binary binary) {
        this.binary = binary;
    }

    public List<ParsedData> getParsed() {
        return parsed;
    }

    public void setParsed(List<ParsedData> parsed) {
        this.parsed = parsed;
    }

       @Override
    public String toString() {
        return "PriceFeed{" +
                "binary=" + binary +
                ", parsed=" + parsed +
                '}';
    }
}


class Binary {
    private List<String> data;
    private String encoding;

    public List<String> getData() {
        return data;
    }

    public byte[][] getBytes() {
        byte[][] byteArray = new byte[data.size()][];

        // Convert each hexadecimal string to a byte array
        for (int i = 0; i < data.size(); i++) {
            byteArray[i] = HexFormat.of().parseHex(data.get(i));
        }

        return byteArray;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String toString() {
        return "Binary{" +
                "data=" + data +
                ", encoding='" + encoding + '\'' +
                '}';
    }
}

class ParsedData {
    @JsonProperty("ema_price")
    private PriceData emaPrice;
    private String id;
    private Metadata metadata;
    private PriceData price;

    public PriceData getEmaPrice() {
        return emaPrice;
    }

    public void setEmaPrice(PriceData emaPrice) {
        this.emaPrice = emaPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public PriceData getPrice() {
        return price;
    }

    public void setPrice(PriceData price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ParsedData{" +
                "emaPrice=" + emaPrice +
                ", id='" + id + '\'' +
                ", metadata=" + metadata +
                ", price=" + price +
                '}';
    }
}

class PriceData {
    private long conf;
    private long expo;
    private long price;
    @JsonProperty("publish_time")
    private long publishTime;

    public long getConf() {
        return conf;
    }

    public void setConf(long conf) {
        this.conf = conf;
    }

    public long getExpo() {
        return expo;
    }

    public void setExpo(long expo) {
        this.expo = expo;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    @Override
    public String toString() {
        return "PriceData{" +
                "conf='" + conf + '\'' +
                ", expo=" + expo +
                ", price='" + price + '\'' +
                ", publishTime=" + publishTime +
                '}';
    }
}

class Metadata {
    @JsonProperty("prev_publish_time")
    private long prevPublishTime;
    @JsonProperty("proof_available_time")
    private long proofAvailableTime;
    private long slot;

    // Getters and setters

    public long getPrevPublishTime() {
        return prevPublishTime;
    }

    public void setPrevPublishTime(long prevPublishTime) {
        this.prevPublishTime = prevPublishTime;
    }

    public long getProofAvailableTime() {
        return proofAvailableTime;
    }

    public void setProofAvailableTime(long proofAvailableTime) {
        this.proofAvailableTime = proofAvailableTime;
    }

    public long getSlot() {
        return slot;
    }

    public void setSlot(long slot) {
        this.slot = slot;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "prevPublishTime=" + prevPublishTime +
                ", proofAvailableTime=" + proofAvailableTime +
                ", slot=" + slot +
                '}';
    }
}
