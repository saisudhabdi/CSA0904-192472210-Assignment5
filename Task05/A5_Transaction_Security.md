# Task 05 – JDBC Transactions and SQL Injection Security

## 1. Wallet transfer problem

A wallet transfer moves an amount from one account to another. Both account updates must succeed together. If either update fails, the complete transfer must be rolled back.

## 2. Transaction steps

1. Obtain a database connection.
2. Disable auto-commit.
3. Verify that the sender has sufficient balance.
4. Debit the sender account.
5. Credit the receiver account.
6. Commit only if both updates succeed.
7. Roll back if any step fails.
8. Restore connection settings and close resources.

## 3. JDBC transaction pseudocode

```java
connection.setAutoCommit(false);

try {
    debitSender(connection, senderId, amount);
    creditReceiver(connection, receiverId, amount);
    connection.commit();
} catch (SQLException e) {
    connection.rollback();
    throw e;
} finally {
    connection.setAutoCommit(true);
}
```

The debit operation should verify that exactly one eligible account row was updated. A conditional update such as balance >= amount prevents a negative balance when used with appropriate transaction isolation and concurrency controls. The application should also validate that the amount is positive and the sender and receiver are different.

## 4. Why auto-commit is unsuitable

With auto-commit enabled, each SQL statement is committed independently. If the debit succeeds but the credit fails, the sender loses money while the receiver does not receive it. A transaction ensures atomicity: either both operations are committed or neither is.

## 5. SQL injection example

Unsafe query construction:

```java
String sql = "SELECT * FROM users WHERE username = '" + username
           + "' AND password = '" + password + "'";
```

If untrusted input is concatenated into SQL, an attacker may alter the query's intended logic.

Safe parameterised query:

```java
String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
PreparedStatement ps = connection.prepareStatement(sql);
ps.setString(1, username);
ps.setString(2, passwordHash);
```

PreparedStatement treats parameter values as data rather than SQL syntax. Passwords should be stored as salted, slow password hashes, not plaintext. Authentication should compare a submitted password using a suitable password-hashing library rather than querying for a plaintext password.

## 6. Additional safeguards

- Use least-privilege database accounts.
- Validate inputs on the server.
- Use foreign keys and appropriate constraints.
- Apply suitable transaction isolation.
- Avoid exposing detailed database errors to end users.
- Use secure credential storage and encrypted database connections where appropriate.
- Log transaction failures without recording passwords or sensitive authentication data.

## 7. Complexity analysis

For a transfer, the application performs a constant number of SQL statements, so the number of application-level operations is O(1). Database execution time depends on indexes, locks, constraints, transaction logging, and the query plan. Indexed account lookups are typically approximately O(log n) in a B-tree index.

## 8. Conclusion

JDBC transactions protect wallet transfers from partial updates. PreparedStatement prevents SQL injection through parameter binding. Together with password hashing, input validation, constraints, and least-privilege access, these practices improve database correctness and security.
