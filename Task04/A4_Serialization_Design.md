# Task 04 – Java Object Serialization

## 1. Objective

Serialization converts an object into a byte stream so that it can be stored in a file or transferred. Deserialization reconstructs the object from that stream.

## 2. Serializable class

A class must implement java.io.Serializable to support standard Java object serialization.

```java
import java.io.Serializable;

public class StudentRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int studentId;
    private String name;
    private transient String databaseConnection;
}
```

serialVersionUID identifies the version of the class used during serialization. Keeping it explicit helps detect incompatible class changes during deserialization.

## 3. Transient fields

A transient field is skipped by default Java serialization. A database Connection should be transient because it represents a live external resource and cannot be meaningfully restored from a saved object stream.

Passwords and authentication tokens should not be persisted in ordinary serialized objects. Marking a field transient prevents default serialization of that field, but it does not replace secure credential management.

After deserialization, transient fields receive default values, such as null for object references. The application must recreate required resources safely.

## 4. Object graph

If a StudentRecord contains an Address object, serialization may also serialize that referenced object if it is serializable. This connected set of referenced objects is called the object graph.

All non-transient objects reachable through the graph must be serializable, or serialization can fail with NotSerializableException. Cyclic references are supported by Java serialization's object-handle mechanism.

## 5. Versioning and compatibility

- Declare a stable serialVersionUID.
- Consider compatibility when adding, removing, or changing fields.
- Validate data after deserialization.
- Handle InvalidClassException and other I/O exceptions.
- Do not deserialize untrusted data because Java native deserialization can expose security risks.
- Prefer a documented schema for long-term interchange.

## 6. CSV or JSON alternative

CSV is suitable for simple tabular data and is easy to inspect, but nested objects and type information require additional conventions. JSON supports nested structures and is widely used for APIs and cross-language data exchange. Both formats are more interoperable than Java's native object serialization.

## 7. Complexity analysis

For an object graph containing s serializable data elements and b bytes of output, serialization and deserialization generally require O(s + b) time and O(b) storage for the resulting stream, subject to object structure and implementation overhead.

## 8. Conclusion

Java serialization can be useful for controlled Java-only applications, but it couples stored data to Java class definitions. For long-term storage, interoperability, and safer data exchange, a validated JSON or CSV format is often preferable.
