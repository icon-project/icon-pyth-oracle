# Pyth Oracle on ICON
Pyth is a oracle blockchain with many data providers(nodes), where in cooperation with wormhole price feed are published and aggregated then signed by the wormhole guardian set.

## Price updates
In other implementation There are 2 contract involved.
1. The wormhole contract. (A pyth chain needs to be wormhole compatible)
2. The pyth accumulator.

On ICON all this is done in the same contract since ICON does not have wormhole or is in any need of the pyth governance logic. This means only the vital logic is in the ICON contracts.
This in turn means that ICON needs to maintain guardian sets ourselves along with protocol changes.

The pyth network have apis where you can subscribe to a set of price feeds and will be delivered to you as a Wormhole vaa proof, a merkle tree of prices and a merkle proof.
This is delivered to the pyth accumulator which extracts the wormhole VAA which is sent to the wormhole contract to be verified.
When verified the pyth contract verifies the merkle proof and extracts the prices and updates the contract.


### Oracle contract
Proof logic taken from:
[Wormhole proof logic Cosmwasm](https://github.com/wormhole-foundation/wormhole/tree/main/cosmwasm/contracts/wormhole/src)
[Pyth merkle proof logic Cosmwasm](https://github.com/pyth-network/pyth-crosschain/tree/main/target_chains/cosmwasm/contracts/pyth/src)
[Pyth merkle proof logic Solidity](https://github.com/pyth-network/pyth-crosschain/tree/main/target_chains/ethereum/contracts/contracts/pyth)


### Backend price delivery
The price backend will follow documentation on how to setup a price delivery system.
How to integrate to price feed: https://docs.pyth.network/price-feeds/use-real-time-data/off-chain

Since this is not permissioned security is  minimized to make a setup as easy as possible and complexity very low.

Example of price backend
```
const { PriceServiceConnection } = require('@pythnetwork/price-service-client');

(async () => {
    const connection = new PriceServiceConnection("https://hermes.pyth.network", {
        priceFeedRequestConfig: {
          // Provide this option to retrieve signed price updates for on-chain contracts.
          // Ignore this option for off-chain use.
          binary: true,
        },
      }); // See Hermes endpoints section below for other endpoints

      const priceIds = [
        // You can find the ids of prices at https://pyth.network/developers/price-feed-ids
        "0xe62df6c8b4a85fe1a67db44dc12de5db330f7ac66b72dc658afedf0f4a415b43", // BTC/USD price id
        "0xff61491a931112ddf1bd8147cd1b641375f79f5825126d665480874634fd0ace", // ETH/USD price id
      ];

    connection.subscribePriceFeedUpdates(priceIds, (priceFeed) => {
        // send to ICON(priceFeed.getVAA())
    });


})();

```


