# TransctID Library Demo App

Demo app to test TransctID library.

https://github.com/netkicorp/transactid-library-java

The goal of this demo app is to test your implementation of the TransctID Library.

## Installation

There are two options for installing and running this project. You can install via the code repository or you can use Docker to launch our image with this way being the easiest.

### Build From Code Repository

#### Prerequisites

To build from the code repository you much have `git`, `jvm` and `gradle` installed on your machine. See your specific distribution instructions on how to install these libraries.

#### Steps

- Clone the repository
- In the root folder of the project execute command

    `./gradlew bootRun`


### Launch From Docker


#### Prerequisites

You must have an installed and running version of `docker`.  See your machine distribution for docker installation instructions.

#### Steps

- Pull image: `docker pull netkicorporate/transactid-java:latest`
- Execute command to run the demo app: `docker run --rm --name transactid-java -p 8080:8080 -d netkicorporate/transactid-java:latest`

If you want to stop the demo app execute

- `docker stop transactid-java`

This will remove the container from your machine and will free up the memory consumed by the container. Every time you want to run the demo app make sure to pull the container again.

## Testing

The app has a UI where you can test the endpoints. Once the app is running you can test using:  

http://localhost:8080/transact-id-docs.html

Also you can implement your own clients to connect to the API.  

## General Usage

## Bip75

### Synchronous flow

Endpoint: `/addresses/{address_id}/messages`  
Description: `Send protocol message binary to this endpoint and receive the correspondent protocol message response synchronous.`  
Verb: `POST`  
Params:
- protocolMessage: `Binary containing protocolMessage`  
Response: `Binary containing protocolMessage`

### Async flow

Endpoint: `/addresses/{address_id}/messages?async=true`  
Description: `This endpoint receives a protocolMessage binary and gives a 202 in return. Asynchronously you will receive the correspondent protocolMessage binary in the URL defined in the protocolMessage`  
Verb: `POST`  
Params:
- invoiceRequest: `Binary containing protocolMessage`  
Response: `HttpStatus 202 or error code`
  
## Testing messages independently

### Synchronous flow

### Initial invoice request

Endpoint: `/initial-invoice-request`  
Description: `Send a GET request to this to receive back an invoiceRequest binary so that you can test parsing thing`  
Verb: `GET`  
Response: `Binary invoiceRequest`  

### Initial invoice request Encrypted

Endpoint: `/initial-invoice-request-encrypted`  
Description: `Send a GET request to this to receive back an invoiceRequest binary encrypted so that you can test parsing thing`  
Verb: `GET`  
Response: `Binary invoiceRequest encrypted`  

Verb: `POST`  
Description: `If you want to test your full flow with getting an invoiceRequest object at your correct endpoint use the POST as described and it will send the binary object to that URL.`  
Params:  
- url: `URL to post invoice-request`  
Response: `HttpStatus 200 or error code`  

### Invoice request

Endpoint: `/invoice-request`  
Description: `Send invoiceRequest binary to this endpoint and receive a paymentRequest binary in return.`  
Verb: `POST`  
Params:  
- invoiceRequest: `Binary containing invoiceRequest`  
Response: `Binary containing paymentRequest`  

### Payment request

Endpoint: `/payment-request`  
Description: `Send a paymentRequest binary to this endpoint and receive a payment binary in return.`  
Verb: `POST`  
Params:  
- paymentRequest: `Binary containing paymentRequest`  
Response: `Binary containing payment`  

### Payment  

Endpoint: `/payment`  
Description: `Send a payment binary to this endpoint and receive a paymentAck binary in return.`  
Verb: `POST`  
Params:  
- payment: `Binary containing payment`  
Response: `Binary containing paymentAck` 

### Asynchronous flow

### Initial invoice request

Endpoint: `/async/initial-invoice-request`  
Description: `If you want to test your full flow with getting an invoiceRequest object at your correct endpoint use the POST as described and it will send the binary object to that URL.`  
Verb: `POST`  
Params:  
- url: `URL to post invoiceRequest` 
Response: `HttpStatus 202 or error code`  

If you want to change the notificationUrl for the InvoiceRequest that will be sent, you can do it in `TransactIdService.NOTIFICATION_URL`

### Initial invoice request Encrypted

Endpoint: `/async/initial-invoice-request-encrypted`  
Verb: `POST`  
Description: `If you want to test your full flow with getting an invoiceRequest object at your correct endpoint use the POST as described and it will send the binary object to that URL.`  
Params:  
- url: `URL to post invoice-request`  
Response: `HttpStatus 202 or error code`  

If you want to change the notificationUrl for the InvoiceRequest that will be sent, you can do it in `TransactIdService.NOTIFICATION_URL`

### Invoice request

Endpoint: `/async/invoice-request`  
Description: `This endpoint receives an invoiceRequest binary and gives a 202 in return. Asynchronously you will receive a PaymentRequest binary in your NotificationUrl defined in your invoiceRequest`  
Verb: `POST`  
Params:  
- invoiceRequest: `Binary containing invoiceRequest`  
Response: `HttpStatus 202 or error code`  

If you want to change the paymentUrl for the PaymentRequest that will be sent, you can do it in `TransactIdService.PAYMENT_URL`

### Payment request

Endpoint: `/async/payment-request`  
Description: `This endpoint receives a paymentRequest binary and gives a 202 in return. Asynchronousluy you will receive a Payment binary in your PaymentUrl defined in your paymentRequest.`  
Verb: `POST`  
Params:  
- paymentRequest: `Binary containing paymentRequest`  
Response: `HttpStatus 202 or error code` 

### Payment  

Endpoint: `/async/payment`  
Description: `Send a payment binary to this endpoint and receive a paymentAck binary in return.`  
Verb: `POST`  
Params:  
- payment: `Binary containing payment`  
Response: `Binary containing paymentAck` 

### Encryption
You can generate EncryptedMessages to test this functionality. Once you start the service you can fetch a set of ECDSA keys to use in your ProtocolMessages.

Endpoint: `/encryption/keys`  
Description: `Send a GET request to this to receive a set of sender/recipient keys to test encrypted messages`  
Verb: `GET`  
Response: `Set of keys to do encryption`  
