# Task B4 – Final Engineering Decision

## Recommended storage

Use a central relational database for the 12-campus transport office. A database supports concurrent counters, indexed searches by pass ID and route, constraints, transactions, backups, and centralised access control.

## Why not a shared text file?

A text file is easy to inspect but concurrent updates require explicit file locking and careful coordination. Searching by pass ID generally requires a sequential scan, and a damaged or partially written file can affect multiple counters.

## Why not Java serialization?

Serialized files are tied to Java class versions and are less suitable for multiple applications or languages. If each campus keeps a separate file, a search across all campuses must inspect each file or maintain a separate index. Concurrent updates and synchronisation between campuses become application responsibilities.

## Database advantages

- A primary key prevents duplicate pass IDs.
- Indexes support efficient lookups.
- Transactions protect multi-step operations.
- Multiple counters can work against the same database.
- Database metadata helps detect differences in database products and JDBC drivers after deployment.

## Complexity comparison

Let n be total passes and c be campuses.

| Design | Search by pass ID | Concurrent updates |
|---|---|---|
| One text file | O(n) sequential scan | Requires file locking and coordination |
| Separate serialized file per campus | O(n) worst case across all files | Requires cross-campus synchronisation |
| Indexed relational database | Typically O(log n) with a B-tree index | Managed using database transactions and locking |

## Migration evidence

The migration program validates each row, writes invalid records to rejected_records.txt, and inserts valid records using PreparedStatement batches inside one transaction. DatabaseMetaData reports the database product, version, JDBC driver, driver version, and table names.

## Final decision

A relational database is the appropriate production choice because the system must serve 12 campuses, support several simultaneous counters, prevent duplicate records, and provide reliable searching and transaction handling. Actual benchmark timings should be recorded on the deployment computer; performance values must not be assumed in advance.
