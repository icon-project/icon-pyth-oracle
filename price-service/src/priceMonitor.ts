import { Price, PriceFeed } from "./priceFeed";
import { ServiceConfig } from "./serviceConfig";
import IconService, { HttpProvider, CallTransactionBuilder, Wallet, SignedTransaction} from 'icon-sdk-js';

export class PriceMonitor {
    private DEFAULT_STEP_LIMIT: number  = 400000000
    private prices: Map<string, Price> = new Map();
    private threshold: number;
    private minInterval: number;
    private processing: boolean = false;
    private provider:HttpProvider;
    private iconService: IconService;
    private wallet: Wallet;
    private address: string;
    private nid: string;
    private stepLimit: number;

    constructor(config: ServiceConfig) {
        this.threshold = config.priceChangeThreshold / 100;
        this.minInterval = config.interval;
        this.provider = new HttpProvider(config.icon_url);
        this.iconService = new IconService(this.provider);
        this.wallet = Wallet.loadPrivateKey(config.icon_pk);
        this.address = config.address;
        this.nid = config.nid;
        this.stepLimit = config.stepLimit || this.DEFAULT_STEP_LIMIT;
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

                if (!await this.updatePrice(feed)) {
                    return
                }

                feed.parsed.forEach(priceEntry => {
                    this.prices.set(priceEntry.id, priceEntry.price)
                })

                return;
            };
        } finally {
            this.processing = false;
        }
    }

    public async updatePrice(feed: PriceFeed): Promise<boolean> {
        const timestamp = (new Date()).getTime() * 1000;
        let tx = new CallTransactionBuilder()
            .nid(this.nid)
            .from(this.wallet.getAddress())
            .stepLimit(this.stepLimit)
            .timestamp(timestamp)
            .to(this.address)
            .method("updatePriceFeed")
            .params({
                "data": feed.binary.data,
            })
            .version("0x3")
            .build();

        const signedTransaction: SignedTransaction = new SignedTransaction(tx, this.wallet);
        const txHash = await this.iconService.sendTransaction(signedTransaction).execute();
        const transactionResult = await this.getTxResult(txHash);
        const res =  transactionResult.status === 1;
        if (!res) {
            console.log(transactionResult)
        }

        console.log(txHash)
        return res
    }

    private async getTxResult(txHash: string): Promise<any> {
        let attempt = 0;
        let maxRetries = 10;
        while (attempt < maxRetries) {
            try {
                const result = await this.iconService.getTransactionResult(txHash).execute();
                return result; // If the function is successful, return the result
            } catch (error) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw new Error(`Failed to resolve ${txHash}: ${error}`);
                }
                await new Promise(resolve => setTimeout(resolve, 1000)); // Wait before retrying
            }
        }

        throw new Error(`Failed after ${attempt} attempts`);
    }
}
