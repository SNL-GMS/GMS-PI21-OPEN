# <sup> GMS Frameworks </sup><br>**Object Transmission and Storage**

The term **Domain Object** describes a Java class which represents
some specific entity in larger domain model. A `SignalDetection` or
`EventHypothesis` would be example domain objects in the GMS domain.
Since **Domain Objects** are accessible via the **Common Object
Interface** they are often referred to as **COI** objects.

**The GMS preference is to have simple domain objects that act as
informational containers with no business logic.**  

The frameworks goal is to support writing a single class definiton
which can be used not only as a data container, but also directly as a
*Data Transmission Object* (DTO).  For database persistence, a *Data
Access Object* (DAO) which mirrors the structure of the DTO is often
required.

## Object Annotations

The following COTS libraries are used for annotating an *abstract*
class to produce a single domain object that can be used
***directly*** for data transmission and persistence:

* [**Autovalue**](https://github.com/google/auto)
  provide annotations used to generate consistent *immutable* value objects
  from simple *abstract* class definitions.
* [**Jackson Annotations**](https://github.com/FasterXML/jackson-annotations)
  are used to to automatically encode and decode immutable objects to and
  from [JSON](https://en.wikipedia.org/wiki/JSON) for data transmission.
* [**BSON Library**](https://mongodb.github.io/mongo-java-driver/)
  provides annotations used to automatically encode and decode
  immutable objects to and from
  [BSON](https://en.wikipedia.org/wiki/BSON) for data persistence.

### Domain Object Definition Using Immutables

Consider a notional **Signal Detector** domain object.

For this example, we want a simple data container with an unique ID, a
station name, and a list of `SignalDetectionHypothesis`. To implement
this domain object, we would define the following *abstract* class in Java
and annotate with `Immutable` annotations.

**SignalDetection.java**
```java
 1. import com.google.auto.value.AutoValue;
 2:
 4: @AutoValue
 5: public abstract class SignalDetection {
 6:   public abstract UUID getId();
 7:   public abstract String getStationName();
 8:   public abstrct List<SignalDetectionHypothesis> getHypothesis();
 9: }
```

* The `@AutoValue` annotation marks this class as an **Immutable**. At
  compile-time **AutoValue** will processes this annotation to generate
  an `AutoValue_SignalDetection` implementation class that extends this
  *abstract* class. Static create o

The resulting generated `ImmutableSignalDetection` class would be the
implementation of the abstract class that would be used in development.

### Data Transfer Extensions Using Jackson

The `@JsonSerialze` and `@JsonDeserialize` annotations are a
convenience supported by **Immutables** which will inject **Jackson**
`@JsonCreator` and `@JsonProperty` for each member variable in the
generated class. Here is the `SignalDetector` example again shown with
the additional annotations.

**SignalDetection.java**
```java
 1: import com.fasterxml.jackson.annotation.*;
 2. import com.google.auto.value.AutoValue;
 3:
 5: @AutoValue
 6: public abstract class SignalDetection {
 7:   public abstract UUID getId();
 8:   public abstract String getStationName();
 9:   public abstarct List<SignalDetectionHypothesis> getHypothesis();
10:
11:   @JsonCreator
12:   public static SignalDetection from(
13:       @JsonProperty("id") UUID id,
14:       @JsonProperty("stationName") String stationName,
15:       @JsonProperty("hypothesis") List<SignaDetectionHypothesis> hypothesis) {
16:   }
17: }
```

**There is still active prototyping work being done in this area. Stay tuned for additional developments.**

