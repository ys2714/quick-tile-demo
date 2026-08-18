# quick-tile-demo
used to research on coexist of android quick tile and zebra software

## Gradle Tasks

### Release build (`releaseApk`)

Builds the release APK, signs it with the `quick-tile-demo-key` release key, renames it
using the current git tag, and copies it to the project root as `quick-tile-demo-<tag>.apk`.

```bash
./gradlew releaseApk
```

On Windows:

```bash
gradlew.bat releaseApk
```

**Prerequisites**

- A release keystore must exist and be referenced from `keystore.properties` (kept out of
  version control) at the project root, with the following keys:

  ```properties
  storeFile=keystore/quick-tile-demo-key.jks
  storePassword=<store password>
  keyAlias=quick-tile-demo-key
  keyPassword=<key password>
  ```

- The project must be checked out with git history/tags available, since the output file
  name is derived from `git describe --tags --always`.

The signed APK will appear at the project root, e.g. `quick-tile-demo-v0.1.apk`.
