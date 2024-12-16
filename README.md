# custom crafter  
custom crafter is a plugin for PaperMC servers that provides custom recipes. 

---

# Recommended environment
- Paper
  - v5.0 (=api v0.1): `1.21.3~`
  - v4.3: `1.21.3`
  - v4.2: `1.20.1 - 1.20.4`

Tested in Paper 1.21.3, 1.20.4.

---

# Features
- Set custom recipes to vanilla items.
- Set custom recipes to original items.
- Provide some commands that manages items that material of recipes.
- Provide those recipes to players.
- (custom crafter provides crafting vanilla items from vanilla recipes feature.)

---

# API

## Documents
[KDoc](https://sakaki-aruka.github.io/custom-crafter/)

## Dependency Information

**Note: `<version>` must be real version string or `master-SNAPSHOT`.**

Maven  
(repository)
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
(dependency)
```xml
<dependency>
    <groupId>com.github.Sakaki-Aruka</groupId>
    <artifactId>custom-crafter</artifactId>
    <version>5.0.2</version>
</dependency>
```


Gradle  
(repository)
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
(dependency)
```groovy
dependencies {
        implementation 'com.github.Sakaki-Aruka:custom-crafter:5.0.2'
}
```

---

# Getting Started
0. Make a plugin
   1. create plugin that uses custom-crafter API

1. Install
   1. [Download from here.(GitHub release page)](https://github.com/Sakaki-Aruka/custom-crafter/releases/latest)
   2. Place the downloaded file to plugins directory. 
   3. Reboot or reload your server.

    
2. Place base block  
custom crafter does not work only a work bench block.  
If you want to use custom crafter features, place the base blocks under a work bench block 3 * 3.  
The default base block is `GOLD_BLOCK`.

---

# LICENSE
MIT License

Copyright (c) 2023 - 2024 Sakaki-Aruka

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
