# Task 03 – Data Access Object (DAO) Pattern

## 1. Objective

The DAO pattern separates database operations from business logic. It provides a consistent interface for creating, reading, updating, and deleting records.

## 2. Generic DAO interface

```java
public interface Dao<T, K> {
    void create(T entity);
    T findById(K id);
    java.util.List<T> findAll();
    void update(T entity);
    void delete(K id);
}
```

Here, T represents the entity type and K represents the type of its primary key.

## 3. Student entity and StudentDao

A Student entity may contain studentId, name, department, and email. StudentDao implements Dao<Student, Integer> and contains SQL statements for the Student table.

Responsibilities of StudentDao:
- Insert a student using PreparedStatement.
- Find a student by studentId.
- Retrieve all students.
- Update student details.
- Delete a student by studentId.
- Convert database rows into Student objects.
- Handle SQL exceptions and resource cleanup.

## 4. Object-to-table mapping

| Java object | Database table |
|---|---|
| Student | Student |
| studentId | student_id |
| name | name |
| department | department |
| email | email |

Each database row is mapped to one Student object. The primary key identifies the object. SQL NULL values must be handled appropriately when mapping columns to Java fields.

## 5. Typical flow

1. The user interface receives a request.
2. The service layer validates business rules.
3. The service calls StudentDao.
4. StudentDao executes a parameterised SQL query.
5. The DAO maps the ResultSet to an object.
6. The result returns to the service and user interface.

## 6. Why not put SQL directly in the application?

Direct SQL in every user-interface method mixes presentation, business rules, and persistence. It creates duplicated queries and makes testing and database changes harder. DAO centralises persistence logic and allows the service layer to remain independent of SQL details.

## 7. Complexity analysis

Let n be the number of student rows.

| Operation | Typical database complexity |
|---|---|
| Insert | Usually O(log n) or database-dependent, including index maintenance |
| Find by indexed primary key | Approximately O(log n) for a B-tree index |
| Find all | O(n) |
| Update by indexed primary key | Approximately O(log n), plus update cost |
| Delete by indexed primary key | Approximately O(log n), plus index maintenance |

Actual performance depends on indexes, database engine, query plan, and storage.

## 8. Conclusion

The DAO pattern improves maintainability, testability, and separation of concerns. A generic Dao<T,K> interface provides common operations, while StudentDao contains the SQL and mapping logic specific to Student records.
