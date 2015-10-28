# mule-object-store-jdbc

An object store implementation which persists to a db. Supports `allKeys` operation (as opposed to `org.mule.transport.jdbc.store.JdbcObjectStore`).

To depend on this project, add the following repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

and the following dependency (where the version is a release tag - or a short commit hash; see [JitPack](https://jitpack.io/) for more information):

```xml
<dependency>
    <groupId>com.github.Ricston</groupId>
    <artifactId>mule-object-store-jdbc</artifactId>
    <version>Release tag</version>
</dependency>
```

You can go [here](https://jitpack.io/#Ricston/mule-object-store-jdbc) for logs when this project is built on JitPack's servers.

See [JitPack](https://jitpack.io/) for more information.
