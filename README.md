# CSA0904 – Assignment 5: Data Persistence Engineering

**Student:** G. Sai Sudhabdi  
**Register Number:** 192472210  
**Course:** Programming in Java – CO5  
**Repository:** https://github.com/saisudhabdi/CSA0904-192472210-Assignment5

## Requirements

- Java JDK 17 or later
- MySQL Server 8.x
- MySQL Connector/J JAR
- Windows Command Prompt or PowerShell

Check Java:

```bat
java -version
javac -version
```

Download MySQL Connector/J from the official MySQL Connector/J download page and place the JAR in the repository root. The examples below use the placeholder name `mysql-connector-j.jar`; replace it with the actual downloaded filename.

Set database connection details in Command Prompt before running JDBC programs:

```bat
set DB_URL=jdbc:mysql://localhost:3306/buspass_test
set DB_USER=root
set DB_PASSWORD=your_mysql_password
```

Do not upload real database passwords to GitHub.

## Part A – Design and pseudocode

| Folder | Contents |
|---|---|
| Task01 | A1 file format design and pseudocode |
| Task02 | A2 buffered I/O design |
| Task03 | A3 DAO pattern design |
| Task04 | A4 serialization design |
| Task05 | A5 transaction and SQL injection design |

Part A is intentionally submitted as design documents and pseudocode, as required by the assignment.

## Part B – Java implementation

### Task 06 – File store and line counter (no MySQL required)

Open Command Prompt in the repository folder:

```bat
cd Task06
javac PassStore.java
java PassStore
javac LineCounter.java
java LineCounter
```

PassStore demonstrates add, find, route listing, and skipping damaged rows. LineCounter generates 1 MB, 10 MB and 50 MB files and measures unbuffered and buffered reads. Timings depend on the computer and disk; record the values printed by your own execution.

### Task 07 – Adversarial inputs (MySQL required)

Run `Task07/database.sql` in MySQL Workbench or the MySQL command-line client.

From the repository root:

```bat
javac -cp ".;mysql-connector-j.jar" Task07\AdversarialInputTest.java
java -cp ".;mysql-connector-j.jar;Task07" AdversarialInputTest
```

The program compares an intentionally unsafe SQL query with a PreparedStatement query and checks malformed CSV records. The unsafe query is for a local demonstration only and must never be used in a real login system.

### Task 08 – JDBC insertion comparison (MySQL required)

Run `Task08/database.sql` first.

```bat
javac -cp ".;mysql-connector-j.jar" Task08\PassLoader.java Task08\PassBenchmark.java
java -cp ".;mysql-connector-j.jar;Task08" PassBenchmark
```

The benchmark compares Statement, PreparedStatement, and PreparedStatement batch insertion for 10, 1,000, 10,000 and 100,000 rows. It also prints column metadata and demonstrates auto-commit versus rollback. Save your actual timings; they vary by computer and MySQL configuration.

### Task 09 – File-to-database migration (MySQL required)

Run `Task09/database.sql` first. The sample input is `Task09/passes.csv`.

```bat
javac -cp ".;mysql-connector-j.jar;Task08" Task08\PassLoader.java Task09\Migration.java
java -cp ".;mysql-connector-j.jar;Task08;Task09" Migration Task09\passes.csv
```

The migration validates records, reports rejected rows in `rejected_records.txt`, loads valid records in a transaction, and displays database and JDBC driver metadata.

## Evidence and submission checklist

- Execute each Java program on your own computer.
- Capture screenshots showing the command, program output, and relevant database results.
- Record actual benchmark timings in your report. Do not invent or reuse another computer's timings.
- Keep the MySQL Connector/J JAR and passwords out of the public repository.
- Verify that all source files compile and that SQL scripts run successfully before submission.

## Important

The Java files and SQL scripts are implementation materials. Successful GitHub upload does not prove that a program has been executed on your computer. Complete the execution and evidence checklist before submitting.
