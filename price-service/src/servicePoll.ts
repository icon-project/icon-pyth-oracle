import { PriceMonitor } from "./priceMonitor";

const { HermesClient } = require('@pythnetwork/hermes-client');
const fs = require('fs');

const config = JSON.parse(fs.readFileSync('config.json', 'utf8'));
const priceMonitor = new PriceMonitor(config);
const connection = new HermesClient(config.pyth_url);

async function poll() {
    const priceUpdates = await connection.getLatestPriceUpdates(config.priceIds);
    priceMonitor.updatePrice(priceUpdates)
}

setInterval(poll, config.interval*1000)
