import { Price, PriceFeed } from "./priceFeed";
import IconService, { HttpProvider, CallTransactionBuilder, Wallet, SignedTransaction} from 'icon-sdk-js';


export class PriceMonitor {
    private prices: Map<string, Price> = new Map();
    private threshold: number;
    private minInterval: number;
    private processing: boolean = false;
    private provider:HttpProvider;
    private iconService: IconService;
    private wallet: Wallet;
    private address: string;
    private nid: string;

    constructor(config: any) {
        this.threshold = config.priceChangeThreshold / 100;
        this.minInterval = config.interval;
        this.provider = new HttpProvider(config.icon_url);
        this.iconService = new IconService(this.provider);
        this.wallet = Wallet.loadPrivateKey(config.icon_pk);
        this.address = config.address;
        this.nid = config.nid;
    }

    public async onPriceUpdate(feed: PriceFeed): Promise<void> {
        if (this.processing) {
            return;
        }

        this.processing = true

        try {
            for (let priceEntry of feed.parsed) {
                let lastUpdate = this.prices.get(priceEntry.id);

                if (lastUpdate) {
                    const priceChange = Math.abs(priceEntry.price.price - lastUpdate.price) / lastUpdate.price;
                    const timeSinceLastUpdate = priceEntry.price.publish_time - lastUpdate.publish_time
                    if (priceChange < this.threshold && timeSinceLastUpdate < this.minInterval) {
                        continue;
                    }
                }

                await this.updatePrice(feed);
                feed.parsed.forEach(priceEntry => {
                    this.prices.set(priceEntry.id, priceEntry.price)
                })
                return;
            };
        } finally {
            this.processing = false;
        }
    }

    public async updatePrice(feed: PriceFeed): Promise<void> {
        const timestamp = (new Date()).getTime() * 1000;
        let tx = new CallTransactionBuilder()
            .nid(this.nid)
            .from(this.wallet.getAddress())
            .stepLimit(400000000)
            .timestamp(timestamp)
            .to(this.address)
            .method("updatePriceFeed")
            .params({
                "data": feed.binary.data,
            })
            .version("0x3")
            .build();

        const signedTransaction: SignedTransaction = new SignedTransaction(tx, this.wallet);
        const res = await this.iconService.sendTransaction(signedTransaction).execute();
        console.log(res)
    }
}
