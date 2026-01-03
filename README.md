# Webhook Commander

This project provides a Fabric mod that makes it easy to run Minecraft commands from external services.

If you only want to use the mod (no development needed):

1. Open the Releases page in your web browser:

    https://github.com/tmih06/TiktokIntegration/releases

2. Download the newest JAR file from the latest release (click the release, then the file under "Assets").

3. Place the downloaded JAR into your Minecraft server's `mods` folder. If you run Minecraft locally with Fabric, put it in the instance's `mods` folder instead.

4. Start or restart your server/game. The mod will create a small config file at `config/webhook_commander.json` on first run.

That is all — the mod will listen on a port and accept requests. If you need to change the port or add a token for security, open `config/webhook_commander.json` with any text editor and edit `port` or `authToken`.

Need help or want to report a problem? Open an issue on the repository: https://github.com/tmih06/TiktokIntegration/issues

License: MIT

---

Developer guide

This section is for developers who want to build, run or release the project.

Prerequisites

-   Java 21 (or newer) installed and `java` on your PATH.
-   Git and the Gradle wrapper are included in the repo; no global Gradle install required.
-   An IDE that supports Gradle (IntelliJ IDEA recommended).

Common tasks

-   Build the mod (produce JAR):

    ```bash
    ./gradlew clean build
    ```

-   Build artifacts only (assemble):

    ```bash
    ./gradlew assemble
    ```

-   Run Minecraft client for development (uses Fabric Loom tasks):

    ```bash
    ./gradlew runClient
    ```

-   Run a dedicated server for testing:

    ```bash
    ./gradlew runServer
    ```

IDE setup

-   Open the project as a Gradle project in your IDE. Import Gradle project files and allow the IDE to download dependencies.
-   Configure project SDK to Java 21.

Configuration

-   The mod writes `config/webhook_commander.json` on first run. Edit `port` and `authToken` there to change the webhook port or enable a bearer token.

Debugging and logs

-   Run `./gradlew runClient` from the terminal to get console logs.
-   Use the IDE run configuration to attach a debugger to the running Minecraft client.

Testing

-   If the project includes tests, run:

    ```bash
    ./gradlew test
    ```

Releasing

-   Releases are produced by the GitHub Actions workflow in `.github/workflows/release.yml`.
-   The workflow runs only when you push a tag matching `v*` (for example `v1.0.0`). To create a release locally and trigger CI:

    ```bash
    git tag v1.0.0
    git push origin v1.0.0
    ```

-   The workflow builds the project and attaches `build/libs/*.jar` to the GitHub release under Assets.

Repository tips

-   Keep changes focused and open a pull request for review.
-   Use the Gradle wrapper (`./gradlew`) so builds are consistent across environments.

Troubleshooting

-   If Gradle fails due to memory, increase JVM args in `gradle.properties` or use `ORG_GRADLE_JAVA_OPTS` environment variable.
-   If dependencies fail to download, check your network or proxy settings.

Where to find code

-   Main source: `src/main/java`
-   Resources (mod metadata): `src/main/resources`
-   Build configuration: `build.gradle` and `gradle.properties`

Contact

-   Open issues on GitHub for problems or feature requests: https://github.com/tmih06/TiktokIntegration/issues
