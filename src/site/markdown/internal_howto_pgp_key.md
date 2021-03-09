# HowTo manage PGP keys

This page is more an internal howto, to remind of the step to manage the PGP keys.

It's an extract from [https://central.sonatype.org/pages/working-with-pgp-signatures.html](https://central.sonatype.org/pages/working-with-pgp-signatures.html)

## Install PGP (windows)

For windows: [https://www.gpg4win.org](https://www.gpg4win.org).

If non admin, the configuration stuff and the keys are stored in _C:\Users\XXXXXXXX\AppData\Roaming\gnupg_

To work behing a proxy, add this line at the end of the _gpg.conf_ file:

keyserver-options http-proxy=http://localhost:3128/ 

## Create and check the key

gpg --gen-key

gpg --list-keys

## Publish the key

gpg --keyserver hkp://pool.sks-keyservers.net --send-keys THE_DATA_OF_THE_KEY

THE_DATA_OF_THE_KEY is listed when using _gpg --list-keys_


As it seems it doesn't work, the easiest way to publish the key is to do it manually:
* Open the ASCCI file
* Paste it in [this page](http://pool.sks-keyservers.net/)
* Click on _Submit the key_, and it's done


On 2020, August the 23th, there seems to be a problem with  pool.sks-keyservers.net. New PGP keys have been published on keyserver.ubuntu.com
 