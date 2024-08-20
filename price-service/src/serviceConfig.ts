export interface ServiceConfig {
    pyth_url: string;
    icon_url: string;
    icon_pk: string;
    address: string;
    nid: string;
    stepLimit: number;
    priceIds: string[];
    priceChangeThreshold: number;
    interval: number;
}