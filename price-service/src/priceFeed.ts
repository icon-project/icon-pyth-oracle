export interface BinaryData {
    encoding: string;
    data: string[];
  }

  export interface Price {
    price: number;
    conf: string;
    expo: number;
    publish_time: number;
  }

  export interface EmaPrice {
    price: string;
    conf: string;
    expo: number;
    publish_time: number;
  }

  export interface Metadata {
    slot: number;
    proof_available_time: number;
    prev_publish_time: number;
  }

  export interface PriceEntry {
    id: string;
    price: Price;
    ema_price: EmaPrice;
    metadata: Metadata;
  }

  export interface PriceFeed {
    binary: BinaryData;
    parsed: PriceEntry[];
  }
