![Latest Release](https://img.shields.io/github/release/ljmc2000/noiselooper.svg?logo=github)
[![Fdroid](https://img.shields.io/f-droid/v/ie.delilahsthings.soothingloop.svg?logo=F-Droid)](https://f-droid.org/en/packages/ie.delilahsthings.soothingloop/)
[![License](https://img.shields.io/github/license/ljmc2000/noiselooper.svg)](https://github.com/ljmc2000/noiselooper/blob/main/LICENSE)

# Description
Play soothing sound effects to help you relax and drown out background noise. Intended to be an android port of [Blanket](https://github.com/rafaelmardojai/blanket).

# Install
* Get it on [F-Droid](https://f-droid.org/en/packages/ie.delilahsthings.soothingloop/) (recommended)
* Download an APK from [Releases](https://github.com/ljmc2000/NoiseLooper/releases)

# Building
Just download and import to android studio. No extra libraries should be needed.

# Adding new sounds
To add a sound called "FOO"
* Import a vector asset called `FOO.xml` to drawable. Android studio will allow you to import SVGs for this end.
* Add a line like `<string name="FOO">The Sound of FOO</string>` to strings.xml.
* Add `FOO.ogg` to the raw assets list.
* Add an entry in `credits.xml`. No really, that's not optional, the list in MainActivity.java is loaded from there.

# Credits
Big thank you to [Rafael Mardojai](https://github.com/rafaelmardojai) (the author of [Blanket](https://github.com/rafaelmardojai/blanket)) and his volenteers, who designed the icons and sourced the sounds. Licences and credits for all sounds are listed in [credits.xml](app/src/main/res/raw/credits.xml).

