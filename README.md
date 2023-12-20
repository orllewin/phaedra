![Phaedra](./assets/phaedra_web_175x165.png)

# Phaedra

A Gemini Protocol client that'll run on any Android device no matter how old:
* There are no dependencies (There is now one dependency, see Conscrypt below)
* It's written in Java
* The compiled .apk is around 55Kb (well, it was, adding the Conscrypt library has made it over 3mb!)
* .apk export compiles in under 5 seconds
* There are no code patterns, and it's been a while since I last coded Java
* This is an experiment

For a full-featured Android client try Phaedra's bigger sibling: [Ariane](https://oppen.digital/software/ariane/)

## Conscrypt

The only thing stopping the first wave of Android devices from 2008 browsing Geminispace is that their TLS versions and crypto suites will be hopelessly outdated. Adding the [Conscrypt library](https://github.com/google/conscrypt) adds support for newer TLS on devices running Gingerbread (API 9) and newer. That still leaves Android 1.0, Cupcake, Donut, Eclair, and Froyo unable to access Geminispace without a change in the Gemini Protocol.

## Licence

[European Union Public Licence v. 1.2](LICENSE)