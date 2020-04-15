#/bin/sh

openssl x509 -inform pem -outform der -in ca.intermediate.cert.pem -out ca.intermediate.cert.der
