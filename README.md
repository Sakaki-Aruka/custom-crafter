# custom crafter  
custom crafter is a plugin for PaperMC servers that provides custom recipes. 

---

# Features
- Set custom recipes to vanilla items.
- Set custom recipes to original items.
- Provide some commands that manages items that material of recipes.
- Provide those recipes to players.
- (custom crafter provides crafting vanilla items from vanilla recipes feature.)

---

# Support versions
Paper
- v5.0.x: `1.21.3 -`
- v4.3: `1.21.3`
- v4.2: `1.20.1 - 1.20.4`

Tested in Paper 1.21.3, 1.20.4.

**WARNING: custom crafter does not support to run on Spigot servers.**

---

# API

custom crafter works API and also a plugin since version 5.0.0 .  
You can make custom recipes in your plugin and register those.  

## Documents
[KDoc](https://sakaki-aruka.github.io/custom-crafter/)  
You can build a document what type of JavaDoc with `mvn dokka:javadoc` on the project root.

## Dependency Information

**Note: the version name must be a real version string or `master-SNAPSHOT`.**  

The latest provided version  
[![](https://jitpack.io/v/Sakaki-Aruka/custom-crafter.svg)](https://jitpack.io/#Sakaki-Aruka/custom-crafter)


<details><summary>Maven</summary>

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
<dependencies>
   <dependency>
      <groupId>com.github.Sakaki-Aruka</groupId>
      <artifactId>custom-crafter</artifactId>
      <version>5.0.8</version>
   </dependency>
</dependencies>

```

</details>

<details><summary>Gradle (Groovy) </summary>

### Gradle (Groovy)  
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
        implementation 'com.github.Sakaki-Aruka:custom-crafter:5.0.8'
}
```

</details>

<details><summary>Gradle (Kotlin DSL)</summary>

(repository)
```
repositories { 
    mavenCentral()
    maven("https://jitpack.io") {
        name = "jitpack"
    }
}
```

(dependency)
```
dependencies {
    implementation("com.github.Sakaki-Aruka:custom-crafter:5.0.8")
}
```

</details>

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

Copyright (c) 2023 - 2025 Sakaki-Aruka

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
