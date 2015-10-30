# mule-object-store-jdbc

An object store implementation which persists to a db. Supports `allKeys` operation (as opposed to `org.mule.transport.jdbc.store.JdbcObjectStore`).

You can optionally set a transformer which transforms to a Serializable. E.g. of config:

```xml
<byte-array-to-object-transformer name="Global_Byte_Array_to_Object" doc:name="Byte Array to Object"/>

<spring:beans>
    <spring:bean name="jdbcObjectStore" class="com.ricston.jdbc.objectstore.TransformableJdbcObjectStore" id="jdbcObjectStore">
        <spring:property name="transformer" ref="Global_Byte_Array_to_Object" />
        <spring:property name="jdbcConnector" ref="jdbcConnector" />
        <spring:property name="insertQueryKey" value="insertQueryKey" />
        <spring:property name="selectQueryKey" value="selectQueryKey" />
        <spring:property name="deleteQueryKey" value="deleteQueryKey" />
        <spring:property name="clearQueryKey" value="clearQueryKey" />
        <spring:property name="allKeysQueryKey" value="allKeysQueryKey" />
    </spring:bean>
    <spring:bean id="dataSource" class="com.mysql.jdbc.jdbc2.optional.MysqlDataSource" name="dataSource">
        <spring:property name="url"
            value="jdbc:mysql://localhost:3306/objectstoreTest?user=objectstore3&amp;password=objectstore3" />
    </spring:bean>
</spring:beans>

<jdbc-ee:connector name="jdbcConnector" dataSource-ref="dataSource" transactionPerMessage="false"
    doc:name="Database" queryTimeout="-1" validateConnections="true">
    <jdbc-ee:query key="insertQueryKey" value="insert into ${objectstore.tablename} (${objectstore.keycolumnname},${objectstore.valuecolumnname}) values (?,?)"></jdbc-ee:query>
    <jdbc-ee:query key="selectQueryKey" value="select ${objectstore.keycolumnname},${objectstore.valuecolumnname} from ${objectstore.tablename} where ${objectstore.keycolumnname} = ?"></jdbc-ee:query>
    <jdbc-ee:query key="deleteQueryKey" value="delete from ${objectstore.tablename} where ${objectstore.keycolumnname} = ?"></jdbc-ee:query>
    <jdbc-ee:query key="clearQueryKey" value="delete from ${objectstore.tablename}"></jdbc-ee:query>
    <jdbc-ee:query key="allKeysQueryKey" value="select ${objectstore.keycolumnname} from ${objectstore.tablename}"></jdbc-ee:query>
</jdbc-ee:connector>

<objectstore:config name="jdbcObjectStoreConfig" objectStore-ref="jdbcObjectStore" doc:name="jdbcObjectStoreConfig" />
```

An example of the expected database table:

```sql
CREATE TABLE IF NOT EXISTS `objectstoreTest`.`objectstore` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `k` VARCHAR(60) NULL,
  `v` BLOB NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `k_UNIQUE` (`k` ASC))
ENGINE = InnoDB;
```

---

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

# Developing locally

To make changes and depend on these changes, checkout the repo, change the version to "<whatever-it-is>-SNAPSHOT" in pom.xml, and after making your changes, install locally with `mvn install`. Then pull it in the project you're using this in with your version.

When ready, push to your fork, release it, and consider issuing a pull request.

# Installing sources locally

To install the source code in your local repo, checkout this project and run: `mvn install` on it. If Studio does not pick up the sources, you can right click the jar under "Referenced Libraries" in "Package Explorer" view, click "Properties", use M2_REPO as a variable in "Location variable path", and then choose the sources jar from the "Extension" button (they should be visible in the UI element in "Extension" if the sources were installed properly).

If this doesn't work (it didn't first time through for me), you might want to try "Build Path" -> "Configure Build Path" and try the same thing from there...

