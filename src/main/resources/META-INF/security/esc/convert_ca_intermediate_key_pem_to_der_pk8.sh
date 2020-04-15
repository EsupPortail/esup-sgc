#/bin/sh

# pass phrase : esup

openssl pkcs8 -topk8 -inform pem -outform der -in ca.intermediate.key.pem -out ca.intermediate.key.der -nocrypt
