import { PriceFeedMetadata } from "@pythnetwork/hermes-client";
import { PriceMonitor } from "./priceMonitor";

const { HermesClient } = require('@pythnetwork/hermes-client');
const fs = require('fs');

const config = JSON.parse(fs.readFileSync('config.json', 'utf8'));
const priceMonitor = new PriceMonitor(config);
(async () => {
    try {
        const connection = new HermesClient(config.pyth_url);
        const priceFeeds: PriceFeedMetadata[] = await connection.getPriceFeeds({
        });
        const filteredData = priceFeeds.filter(item => config.priceIds.includes("0x"+item.id));
        console.log(filteredData);

        const eventSource = await connection.getPriceUpdatesStream(config.priceIds);
        eventSource.onmessage = (event: MessageEvent) => {
            const data = JSON.parse(event.data);
            priceMonitor.onPriceUpdate(data);
        };

        eventSource.onerror = (error: any) => {
            console.log("Data stream failed: "+ error)
            process.exit(1);
        };

    } catch (error) {
        console.log("Data stream failed: "+ error)
        process.exit(1);
    }
})();