# Price monitor

## Start polling service
``` bash
npm run start-poll
```

## Start streaming service
``` bash
npm run start-stream
```
## Configuring service

- **pyth_url**: The URL for the Pyth network hermes.
  - Example: `https://hermes.pyth.network`

- **icon_url**: The URL for the ICON network API.
  - Example: `https://lisbon.net.solidwallet.io/api/v3`

- **icon_pk**: Wallet private key for the ICON network.
  - Example: `""`

- **address**: The blockchain address for the contract on the ICON network,
  - Example: `cx7380205103a9076aae26d1c761a8bb6652ecf30f`

- **nid**: Network ID for the ICON network, specifying the network environment (e.g., mainnet, testnet).
  - Example: `0x2`

- **priceIds**: A list of identifiers for specific data feeds provided by the Pyth network. [Feeds](https://pyth.network/price-feeds)
  - Example:
    ``` json
    [
    "0xe62df6c8b4a85fe1a67db44dc12de5db330f7ac66b72dc658afedf0f4a415b43"
    "0xff61491a931112ddf1bd8147cd1b641375f79f5825126d665480874634fd0ace"
    "0xef0d8b6fda2ceba41da15d4095d1da392a0d2f8ed0c6c7bc0f4cfac8c280b56d"
    "0xb7a8eba68a997cd0210c2e1e4ee811ad2d174b3611c22d9ebf16f4cb7e9ba850"
    ]
    ```
- **priceChangeThreshold**: The value price change threshold used in streaming, in %, determining when prices should be updated.
  - Example: `0.2`

- **interval**: The time interval, in seconds, between price updates. In streaming is only used if priceChangeThreshold is not hit within the interval
  - Example: `300`

# How to setup a price delivery service using PM2
To for example setup a price monitor on  a EC2 instance:

## Setup Polling service
``` bash
pm2 start ./node_modules/.bin/ts-node --name "price-monitor" -- src/servicePoll.ts
```
## Setup Streaming service
``` bash
pm2 start ./node_modules/.bin/ts-node --name "price-monitor" -- src/serviceStreaming.ts
```

Configure service to start on startup
``` bash
pm2 save
pm2 startup
```
