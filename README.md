# MicronautBroker

Simple RESTful API for managing BTC trading requests using the [Micronaut](https://micronaut.io/) framework. There is no authentication in this application, while the API only managers request to create accounts and buy assets (BTC).

A simple Flask service producing BTC/USD prices is included
```bash
$pip install -r requirements.txt
$python exchange.py
```
The endpoint providing quotes is located at http://127.0.0.1:5000/btc-price


All API calls are [implemented](https://github.com/massiccio/MicronautBroker/blob/main/src/main/java/org/broker/Broker.java) as GET requests. The following endpoints are exposed at http://localhost:8080/broker
* _/account/create/{name}/{usdBalance}_: Creates an account for the given person and provided USD balance
* _/account/get/{accountId}_: Gets information about the account identified by _accountId_
* _/order/create/{accountId}/{priceLimit}/{amount}_: Creates a new order for the account identified by _accountId_. The limit price as well as the amount of BTC to buy are provided
* _/order/get/{orderId}_: Gets details and status about the order identified by _orderId_
* _/order/all_: Returns the details and status of all orders

### Examples 
1. _broker/account/create/marc/10000_: create a new account for the user "marc" with balance $10,000. The account id is returned
2. _broker/order/create/1/9000/2_: create a new order for account 1. The price limit is $9,000 and the amount of Bitcoins to be purchased is 2.

In other words, account 1 must have a balance of at least $18,000 in order for the order to be executed. Three things may happen:
1. the current price is larger than $9,000: the order remains in PENDING state;
2. the current price is at most $9,000 and the account has a balance of at least $18,000: the order is marked as FILLED, the Bitcoin balance is increased by 2 and the USD balance is decreased by $18,000
3. the current price is at most $9,000 and the account has a balance of less than $18,000: the order is marked as CANCELLED and nothing else happens

## Implementation details
1. The [PriceCheckerTask](https://github.com/massiccio/MicronautBroker/blob/main/src/main/java/org/broker/PriceCheckerTask.java) background task is used to poll the provided price service every 2 seconds (localhost/port 5000).
A simple implementation of the circuit breaker pattern is used in case the price service is not available.
2. Pending orders (the order book) are stored in a (max) heap. Every time a new price is available, all orders whose limit price is larger or equal than the price are executed (if the account balance is sufficient)

## Testing the code
```bash
./gradlew build
```

## Running the service
```bash
./gradlew run
```
