export class API {
    constructor(api_url, api_key) {
        this.api_url = api_url
        this.api_key = api_key
    }

    getSimpleData() {
        return fetch(
            `${this.api_url}/api/punchout/simpleweeklystats`,
            { headers: { ApiKey: this.api_key } }
        );
    }
}