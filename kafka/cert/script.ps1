# Константы
$KEYSTORE_FILENAME = "kafka.keystore.jks"
$VALIDITY_IN_DAYS = 3650
$DEFAULT_TRUSTSTORE_FILENAME = "kafka.truststore.jks"
$TRUSTSTORE_WORKING_DIRECTORY = "truststore"
$KEYSTORE_WORKING_DIRECTORY = "keystore"
$CA_CERT_FILE = "ca-cert"
$KEYSTORE_SIGN_REQUEST = "cert-file"
$KEYSTORE_SIGN_REQUEST_SRL = "ca-cert.srl"
$KEYSTORE_SIGNED_CERT = "cert-signed"

# Функция проверки существования файла и выхода
function File-Exists-And-Exit {
    param (
        [string]$file
    )
    Write-Host "'$file' cannot exist. Move or delete it before re-running this script."
    exit 1
}

# Проверка существования рабочих директорий и файлов
if (Test-Path $KEYSTORE_WORKING_DIRECTORY) {
    File-Exists-And-Exit $KEYSTORE_WORKING_DIRECTORY
}

if (Test-Path $CA_CERT_FILE) {
    File-Exists-And-Exit $CA_CERT_FILE
}

if (Test-Path $KEYSTORE_SIGN_REQUEST) {
    File-Exists-And-Exit $KEYSTORE_SIGN_REQUEST
}

if (Test-Path $KEYSTORE_SIGN_REQUEST_SRL) {
    File-Exists-And-Exit $KEYSTORE_SIGN_REQUEST_SRL
}

if (Test-Path $KEYSTORE_SIGNED_CERT) {
    File-Exists-And-Exit $KEYSTORE_SIGNED_CERT
}

Write-Host ""
Write-Host "Welcome to the Kafka SSL keystore and truststore generator script."

Write-Host ""
Write-Host "First, do you need to generate a trust store and associated private key,"
Write-Host "or do you already have a trust store file and private key?"
Write-Host ""
$generate_trust_store = Read-Host "Do you need to generate a trust store and associated private key? [yn]"

$trust_store_file = ""
$trust_store_private_key_file = ""

if ($generate_trust_store -eq "y") {
    if (Test-Path $TRUSTSTORE_WORKING_DIRECTORY) {
        File-Exists-And-Exit $TRUSTSTORE_WORKING_DIRECTORY
    }

    New-Item -ItemType Directory -Path $TRUSTSTORE_WORKING_DIRECTORY | Out-Null
    Write-Host ""
    Write-Host "OK, we'll generate a trust store and associated private key."
    Write-Host ""
    Write-Host "First, the private key."
    Write-Host ""
    Write-Host "You will be prompted for:"
    Write-Host " - A password for the private key. Remember this."
    Write-Host " - Information about you and your company."
    Write-Host " - NOTE that the Common Name (CN) is currently not important."

    # Генерация случайного файла для OpenSSL
    openssl rand -writerand "$TRUSTSTORE_WORKING_DIRECTORY/.rnd"
    openssl req -new -x509 -keyout "$TRUSTSTORE_WORKING_DIRECTORY/ca-key" -out "$TRUSTSTORE_WORKING_DIRECTORY/$CA_CERT_FILE" -days $VALIDITY_IN_DAYS

    $trust_store_private_key_file = "$TRUSTSTORE_WORKING_DIRECTORY/ca-key"

    Write-Host ""
    Write-Host "Two files were created:"
    Write-Host " - $TRUSTSTORE_WORKING_DIRECTORY/ca-key -- the private key used later to sign certificates"
    Write-Host " - $TRUSTSTORE_WORKING_DIRECTORY/$CA_CERT_FILE -- the certificate that will be stored in the trust store in a moment and serve as the certificate authority (CA)."

    Write-Host ""
    Write-Host "Now the trust store will be generated from the certificate."
    Write-Host ""
    Write-Host "You will be prompted for:"
    Write-Host " - the trust store's password (labeled 'keystore'). Remember this"
    Write-Host " - a confirmation that you want to import the certificate"

    keytool -keystore "$TRUSTSTORE_WORKING_DIRECTORY/$DEFAULT_TRUSTSTORE_FILENAME" -alias CARoot -import -file "$TRUSTSTORE_WORKING_DIRECTORY/$CA_CERT_FILE"

    $trust_store_file = "$TRUSTSTORE_WORKING_DIRECTORY/$DEFAULT_TRUSTSTORE_FILENAME"

    Write-Host ""
    Write-Host "$TRUSTSTORE_WORKING_DIRECTORY/$DEFAULT_TRUSTSTORE_FILENAME was created."

    # Удаление сертификата, так как он уже в хранилище доверия
    Remove-Item "$TRUSTSTORE_WORKING_DIRECTORY/$CA_CERT_FILE"
} else {
    Write-Host ""
    $trust_store_file = Read-Host "Enter the path of the trust store file: "
    if (-not (Test-Path $trust_store_file)) {
        Write-Host "$trust_store_file isn't a file. Exiting."
        exit 1
    }

    $trust_store_private_key_file = Read-Host "Enter the path of the trust store's private key: "
    if (-not (Test-Path $trust_store_private_key_file)) {
        Write-Host "$trust_store_private_key_file isn't a file. Exiting."
        exit 1
    }
}

Write-Host ""
Write-Host "Continuing with:"
Write-Host " - trust store file:        $trust_store_file"
Write-Host " - trust store private key: $trust_store_private_key_file"

New-Item -ItemType Directory -Path $KEYSTORE_WORKING_DIRECTORY | Out-Null

Write-Host ""
Write-Host "Now, a keystore will be generated. Each broker and logical client needs its own keystore."
Write-Host "This script will create only one keystore. Run this script multiple times for multiple keystores."
Write-Host ""
Write-Host "You will be prompted for the following:"
Write-Host " - A keystore password. Remember it."
Write-Host " - Personal information, such as your name."
Write-Host "     NOTE: currently in Kafka, the Common Name (CN) does not need to be the FQDN of this host."

keytool -keystore "$KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME" -alias localhost -validity $VALIDITY_IN_DAYS -genkey -keyalg RSA

Write-Host ""
Write-Host "'$KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME' now contains a key pair and a self-signed certificate."

Write-Host ""
Write-Host "Fetching the certificate from the trust store and storing in $CA_CERT_FILE."
Write-Host ""
Write-Host "You will be prompted for the trust store's password (labeled 'keystore')"

keytool -keystore $trust_store_file -export -alias CARoot -rfc -file $CA_CERT_FILE

Write-Host ""
Write-Host "Now a certificate signing request will be made to the keystore."
Write-Host ""
Write-Host "You will be prompted for the keystore's password."

keytool -keystore "$KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME" -alias localhost -certreq -file $KEYSTORE_SIGN_REQUEST

Write-Host ""
Write-Host "Now the trust store's private key (CA) will sign the keystore's certificate."
Write-Host ""
Write-Host "You will be prompted for the trust store's private key password."

openssl x509 -req -CA $CA_CERT_FILE -CAkey $trust_store_private_key_file -in $KEYSTORE_SIGN_REQUEST -out $KEYSTORE_SIGNED_CERT -days $VALIDITY_IN_DAYS -CAcreateserial

Write-Host ""
Write-Host "Now the CA will be imported into the keystore."
Write-Host ""
Write-Host "You will be prompted for the keystore's password and a confirmation that you want to import the certificate."

keytool -keystore "$KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME" -alias CARoot -import -file $CA_CERT_FILE
Remove-Item $CA_CERT_FILE

Write-Host ""
Write-Host "Now the keystore's signed certificate will be imported back into the keystore."
Write-Host ""
Write-Host "You will be prompted for the keystore's password."

keytool -keystore "$KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME" -alias localhost -import -file $KEYSTORE_SIGNED_CERT

Write-Host ""
Write-Host "All done!"
Write-Host ""
Write-Host "Delete intermediate files? They are:"
Write-Host " - '$KEYSTORE_SIGN_REQUEST_SRL': CA serial number"
Write-Host " - '$KEYSTORE_SIGN_REQUEST': the keystore's certificate signing request (that was fulfilled)"
Write-Host " - '$KEYSTORE_SIGNED_CERT': the keystore's certificate, signed by the CA, and stored back into the keystore"

$delete_intermediate_files = Read-Host "Delete? [yn] "
if ($delete_intermediate_files -eq "y") {
    Remove-Item $KEYSTORE_SIGN_REQUEST_SRL
    Remove-Item $KEYSTORE_SIGN_REQUEST
    Remove-Item $KEYSTORE_SIGNED_CERT
}