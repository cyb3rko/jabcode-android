# JAB Code Android
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://apilevels.com)
[![license](https://img.shields.io/github/license/cyb3rko/jabcode-android)](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html)
[![last commit](https://img.shields.io/github/last-commit/cyb3rko/jabcode-android?color=F34C9F)](https://github.com/cyb3rko/jabcode-android/commits/main)

---

- [About this project](#about-this-project)
- [How to use](#how-to-use)
- [Bad Performance (Workaround)](#bad-performance-workaround)
- [Contribute](#contribute)
- [License](#license)

---

## About this project

> [!WARNING]
> This library is still in development and not recommended for production use.  
> I couldn't figure out yet how to pass the image bytes directly to the native library.  
> The performance is still bad due to a workaround (see [Bad Performance (Workaround)](#bad-performance-workaround) for more information).  
> Help needed!

This library is a Kotlin wrapper around the native JAB Code library implementation (to be found at [jabcode/jabcode](https://github.com/jabcode/jabcode)).  
It utilizes the [JNI (Java Native Interface)](https://de.wikipedia.org/wiki/Java_Native_Interface) and [some lines of custom C code](https://github.com/search?q=repo%3Acyb3rko%2Fjabcode-android++language%3AC&type=code) to communicate with the native implementation.

But what's a JAB Code?
> JAB Code (Just Another Bar Code) is a high-capacity 2D color bar code, which can encode more data than traditional black/white codes. This repository contains a library for reading and writing JAB codes, along with sample applications. A demo webinterface is available at https://jabcode.org.

If you think it's worth to support this project, feel free to give a small donation :heart:.

---

- 🔗 easy to integrate
- 🏋️‍♂️ lightweight library
- 📦 wrapper around orignal native code (no modifications) -> easy to update native library

# How to use

Create a `JabCodeLib` object (which initializes the native library behind the scenes).

```kotlin
val jabCodeLib: JabCodeLib = JabCodeLib()
```

Call the `detect(imagePath: String)` function with a valid absolute file path of the image you want to scan.

```kotlin
val result: ByteArray? = jabCodeLib.detect(file.absolutePath)
```

(Short form for single call):
```kotlin
val result: ByteArray? = JabCodeLib().detect(file.absolutePath)
```

---

There you have the content of the code, or null if nothing was found.  
If you for example saved a string in your JAB Code, you can now read it with

```kotlin
val string = result.decodeToString()
```

## Bad Performance (Workaround)
Unfortunately the current implementation has a very bad performance due to a workaround.  
I couldn't figure out yet how to pass the image bytes directly to the native library.  
Currently the logic looks like this:

<img src="https://i.imgur.com/PncmN6o.jpeg"/>

Ideally the implementation could rather look like this for best performance:

<img src="https://i.imgur.com/XaHrzLg.jpeg"/>

---

Icon Credits

    Font Awesome Free 6.5.1 by @fontawesome - https://fontawesome.com
    License - https://fontawesome.com/license/free
    Copyright 2024 Fonticons, Inc.

## Contribute
Of course, I'm happy about any kind of contribution.

For creating [issues](https://github.com/cyb3rko/jabcode-android/issues) there's no real guideline you should follow.
If you create [pull requests](https://github.com/cyb3rko/jabcode-android/pulls) please try to use the syntax I use.
Using a unified code format makes it much easier for me and for everyone else.

## License

    Copyright (C) 2024 Cyb3rKo

    This program is free software: you can redistribute it and/or modify it under
    the terms of the GNU Lesser General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option) any
    later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
    FOR A PARTICULAR PURPOSE. See the GNU General Lesser Public License for more
    details.

    A copy of the GNU Lesser General Public License can be found alongside this
    library's source code. Alternatively, see at <http://www.gnu.org/licenses/>.
