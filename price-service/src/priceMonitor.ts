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

    constructor(config: any) {
        this.threshold = config.threshold / 100;
        this.minInterval = config.interval;
        this.provider = new HttpProvider(config.icon_url);
        this.iconService = new IconService(this.provider);
        this.wallet = Wallet.loadPrivateKey(config.icon_pk);
    }

    public async onPriceUpdate(feed: PriceFeed): Promise<void> {

        if (this.processing) {
            console.log(`Skipping update`);
            return;
        }

        this.processing = true

        try {
            const parsed = feed.parsed;

            for (let priceEntry of parsed) {
                let lastUpdate = this.prices.get(priceEntry.id);
                if (lastUpdate) {

                    const priceChange = Math.abs(priceEntry.price.price - lastUpdate.price) / lastUpdate.price;
                    console.log(priceChange);

                    if (priceChange >= this.threshold || priceEntry.price.publish_time - lastUpdate.publish_time >= this.minInterval) {
                        await this.updatePrice(feed);
                        return;
                    }
                } else {
                    await this.updatePrice(feed);
                    return;
                }

                return;
            };
        } finally {
            this.processing = false;
        }
    }

    private async updatePrice(feed: PriceFeed): Promise<void> {
        const timestamp = (new Date()).getTime() * 1000;
        let tx = new CallTransactionBuilder()
            .nid("0x2")
            .from(this.wallet.getAddress())
            .stepLimit(400000000)
            .timestamp(timestamp)
            .to("cx7380205103a9076aae26d1c761a8bb6652ecf30f")
            .method("updatePriceFeed")
            .params({
                "data": feed.binary.data,
            })
            .version("0x3")
            .build();

    const signedTransaction: SignedTransaction = new SignedTransaction(tx, this.wallet);
    const res = await this.iconService.sendTransaction(signedTransaction).execute();
    console.log(res);
    const parsed = feed.parsed;
    parsed.forEach(priceEntry => {
        this.prices.set(priceEntry.id, priceEntry.price)
    })
    }
}
