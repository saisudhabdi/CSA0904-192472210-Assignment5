# Task 01 – A1 Pseudocode

## Data structure

Each pass contains passId, passengerName, routeNumber, validFrom, validTo, fare, and active.

## AddPass

```text
PROCEDURE AddPass(pass)
    IF any required field is missing THEN
        DISPLAY "Invalid pass details"
        RETURN
    END IF

    IF pass.fare < 0 OR pass.validFrom > pass.validTo THEN
        DISPLAY "Invalid fare or validity dates"
        RETURN
    END IF

    OPEN bus_passes.txt FOR READING
    FOR EACH line IN file
        record = ParseAndValidate(line)
        IF record is valid AND record.passId = pass.passId THEN
            CLOSE file
            DISPLAY "Duplicate pass ID"
            RETURN
        END IF
    END FOR
    CLOSE file

    OPEN bus_passes.txt FOR APPENDING
    WRITE pass fields separated by "|"
    WRITE newline
    CLOSE file

    DISPLAY "Pass added successfully"
END PROCEDURE
```

## FindPass

```text
PROCEDURE FindPass(requiredId)
    OPEN bus_passes.txt FOR READING

    FOR EACH line IN file
        IF line does not contain exactly seven fields THEN
            LOG damaged line
            CONTINUE
        END IF

        record = ParseAndValidate(line)
        IF record is invalid THEN
            LOG damaged line
            CONTINUE
        END IF

        IF record.passId = requiredId THEN
            CLOSE file
            RETURN record
        END IF
    END FOR

    CLOSE file
    RETURN NOT_FOUND
END PROCEDURE
```

## ListPasses

```text
PROCEDURE ListPasses()
    OPEN bus_passes.txt FOR READING
    validCount = 0
    errorCount = 0

    FOR EACH line IN file
        record = ParseAndValidate(line)

        IF record is valid THEN
            DISPLAY record
            validCount = validCount + 1
        ELSE
            LOG line number and validation error
            errorCount = errorCount + 1
        END IF
    END FOR

    CLOSE file
    DISPLAY validCount, " valid passes"
    DISPLAY errorCount, " damaged records"
END PROCEDURE
```

## ParseAndValidate

```text
FUNCTION ParseAndValidate(line)
    fields = Split line using "|"

    IF number of fields is not 7 THEN
        RETURN INVALID
    END IF

    IF passId, passengerName, or routeNumber is empty THEN
        RETURN INVALID
    END IF

    IF validFrom or validTo is not a valid ISO date THEN
        RETURN INVALID
    END IF

    IF fare is not a valid non-negative decimal THEN
        RETURN INVALID
    END IF

    IF active is neither "true" nor "false" THEN
        RETURN INVALID
    END IF

    RETURN constructed pass record
END FUNCTION
```

## Error-handling policy

A damaged line is logged and skipped during search or listing. The program must not silently modify the source file. Any repair operation should write valid records to a temporary file, verify the result, and retain a backup of the original.
