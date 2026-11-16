# Task 02 – Buffered I/O for a 50 MB Attendance Log

## 1. Problem

The attendance application must process a 50 MB log file efficiently. Reading one byte or one small portion at a time can cause a large number of disk read operations.

## 2. Unbuffered reading

If the program requests one byte per read operation, a 50 MB file contains approximately 52,428,800 bytes. This could result in an extremely high number of read calls. Actual operating-system and hardware behaviour may combine or cache operations, but small application-level reads still create avoidable overhead.

## 3. Buffered reading

Java BufferedReader or BufferedInputStream reads a larger block of data into memory. The application then processes data from this memory buffer. When the buffer becomes empty, another block is read from the file.

Example:

```java
try (BufferedReader reader = new BufferedReader(new FileReader("attendance.log"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        process(line);
    }
}
```

The default buffer size is implementation-dependent. A buffer such as 8 KB or 64 KB can be tested for the workload rather than assuming one size is always best.

## 4. Advantages

- Reduces frequent small read requests.
- Improves throughput for sequential file processing.
- Provides convenient line-by-line processing.
- Keeps memory usage relatively small compared with loading the entire file.

## 5. Limitations

- Buffering does not make random access efficient.
- Very large buffers consume more memory.
- A buffer must be flushed when writing so pending data is not lost.
- Reading a file line by line still requires O(n) time, where n is the number of characters or bytes processed.
- Malformed or extremely long lines still require validation and error handling.

## 6. Files.readAllLines alternative

Files.readAllLines(Path) loads all lines into a List<String>. It is convenient for small files and when the complete dataset must be available at once. For a 50 MB file, memory usage can be much higher than 50 MB because strings, list entries, and object overhead are also stored. It can cause memory pressure on systems with limited RAM.

## 7. Complexity analysis

Let n be the total number of bytes and m be the number of lines.

| Approach | Time complexity | Extra memory |
|---|---|---|
| BufferedReader line-by-line | O(n) | O(buffer size + largest line) |
| BufferedInputStream | O(n) | O(buffer size) |
| Files.readAllLines | O(n) | O(n) |

## 8. Recommendation

Use BufferedReader for a large attendance log when the application processes records sequentially. Validate each line as it is read, maintain counters or aggregates incrementally, and write invalid records to a separate error log. Use Files.readAllLines only when the file is small enough to fit comfortably in memory.
