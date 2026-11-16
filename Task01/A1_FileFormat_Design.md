# Task 01 – File Format Design for Bus Pass Records

## 1. Objective

Design a reliable file format for storing bus pass records. The system must support adding a pass, finding a pass by ID, and listing all passes. It must also detect damaged or incomplete records.

## 2. Record fields

Each bus pass contains:

| Field | Data type | Purpose |
|---|---|---|
| passId | String | Unique pass identifier |
| passengerName | String | Name of the passenger |
| routeNumber | String | Bus route |
| validFrom | Date | Start date of validity |
| validTo | Date | End date of validity |
| fare | Decimal | Pass fee |
| active | Boolean | Whether the pass is active |

## 3. Text file format

Use one record per line. Separate fields using the pipe character (|). Dates use ISO format (YYYY-MM-DD), fare uses two decimal places, and active uses true or false.

Example:

```text
P1001|Ananya Rao|R12|2026-11-01|2026-11-30|850.00|true
P1002|Rahul Kumar|R08|2026-11-05|2026-12-04|920.00|true
```

A text file is human-readable and easy to edit. The application must reject lines with missing fields, invalid dates, invalid fare values, or duplicate pass IDs. If a field can contain the delimiter, it must be escaped or the format must be changed to a structured format such as CSV with quoting or JSON.

## 4. Binary file format

A binary file stores records in a defined sequence. A practical fixed-length layout can use:

| Field | Storage |
|---|---:|
| passId | 16 bytes |
| passengerName | 40 bytes |
| routeNumber | 8 bytes |
| validFrom | 10 bytes |
| validTo | 10 bytes |
| fare | 8-byte double |
| active | 1 byte |

Total record size: 93 bytes.

Strings are encoded as UTF-8 and padded to their allocated byte length. Values longer than the allocated length must be rejected rather than silently truncated. Dates are stored as YYYY-MM-DD. The file should begin with a small header containing a format version and record count.

Fixed-length records allow direct access to record n using:

```text
offset = headerSize + (n × recordSize)
```

## 5. Required operations

### Add a pass

1. Validate all input fields.
2. Check that passId is not already present.
3. Convert the record to the selected file format.
4. Append the record.
5. Flush and close the file safely.

### Find a pass

1. Receive the required passId.
2. Read records one at a time.
3. Validate each record before parsing it.
4. Return the matching record.
5. Return a not-found result if no match exists.

### List all passes

1. Open the file for reading.
2. Read and validate each record.
3. Display valid records.
4. Report damaged records separately without stopping the entire listing.

## 6. Handling damaged or incomplete data

- Check that every text line has exactly seven fields.
- Validate dates, numeric fare, Boolean values, and required fields.
- For binary files, verify the header, version, record count, and expected file length.
- Detect truncated records and invalid field encodings.
- Do not overwrite the original file when repairing it.
- Write valid records to a temporary file, record rejected lines in an error log, and replace the original only after verification.
- Keep a backup before migration or repair.

## 7. RandomAccessFile alternative

Java's RandomAccessFile can read or update a fixed-length binary record without scanning the complete file. After locating a pass, the program calculates its byte offset and uses seek(offset) to access it.

This approach is useful when records have a fixed size and direct access is required. It is less suitable for variable-length records because offsets are harder to calculate and updates may shift later records.

## 8. Complexity analysis

Let n be the number of stored passes.

| Operation | Text file | Fixed-length binary file |
|---|---|---|
| Add | O(n) if duplicate checking scans the file; O(1) append only | O(n) if duplicate checking scans; O(1) append only |
| Find by ID | O(n) | O(n) sequential search; O(1) by record position when the position is already known |
| List all | O(n) | O(n) |
| Direct access by record number | O(n) in a normal sequential text file | O(1) using calculated offset and RandomAccessFile |

## 9. Conclusion

A text file is preferable when readability, portability, and simple debugging are priorities. A fixed-length binary file with RandomAccessFile is preferable when predictable record size and direct record access are important. For this bus pass system, the text format is simpler to maintain, while binary storage is more suitable for a large fixed-record dataset requiring frequent random access.
