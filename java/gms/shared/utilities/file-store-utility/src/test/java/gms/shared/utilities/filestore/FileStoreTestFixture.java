package gms.shared.utilities.filestore;

import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class FileStoreTestFixture {

  static class DeserializedTestClass1 {
    String stringField;
    Double doubleField;
    Map<String, Integer> mapField;

    public DeserializedTestClass1() {

    }

    public DeserializedTestClass1(String stringField, Double doubleField,
      Map<String, Integer> mapField) {
      this.stringField = stringField;
      this.doubleField = doubleField;
      this.mapField = mapField;
    }

    public String getStringField() {
      return stringField;
    }

    public void setStringField(String stringField) {
      this.stringField = stringField;
    }

    public Double getDoubleField() {
      return doubleField;
    }

    public void setDoubleField(Double doubleField) {
      this.doubleField = doubleField;
    }

    public Map<String, Integer> getMapField() {
      return mapField;
    }

    public void setMapField(Map<String, Integer> mapField) {
      this.mapField = mapField;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DeserializedTestClass1)) {
        return false;
      }
      DeserializedTestClass1 that = (DeserializedTestClass1) o;
      return Objects.equals(stringField, that.stringField) && Objects.equals(
        doubleField, that.doubleField) && Objects.equals(mapField, that.mapField);
    }

    @Override
    public int hashCode() {
      return Objects.hash(stringField, doubleField, mapField);
    }

    @Override
    public String toString() {
      return "DeserializedTestClass1{" +
        "stringField='" + stringField + '\'' +
        ", doubleField=" + doubleField +
        ", mapField=" + mapField +
        '}';
    }
  }

  static class DeserializedTestClass2 {
    String stringField;
    Double doubleField;
    Integer integerField1;
    Integer integerField2;

    public DeserializedTestClass2() {

    }

    public DeserializedTestClass2(String stringField, Double doubleField,
      Integer integerField1, Integer integerField2) {
      this.stringField = stringField;
      this.doubleField = doubleField;
      this.integerField1 = integerField1;
      this.integerField2 = integerField2;
    }

    public String getStringField() {
      return stringField;
    }

    public void setStringField(String stringField) {
      this.stringField = stringField;
    }

    public Double getDoubleField() {
      return doubleField;
    }

    public void setDoubleField(Double doubleField) {
      this.doubleField = doubleField;
    }

    public Integer getIntegerField1() {
      return integerField1;
    }

    public void setIntegerField1(Integer integerField1) {
      this.integerField1 = integerField1;
    }

    public Integer getIntegerField2() {
      return integerField2;
    }

    public void setIntegerField2(Integer integerField2) {
      this.integerField2 = integerField2;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DeserializedTestClass2)) {
        return false;
      }
      DeserializedTestClass2 that = (DeserializedTestClass2) o;
      return Objects.equals(stringField, that.stringField) && Objects.equals(
        doubleField, that.doubleField) && Objects.equals(integerField1, that.integerField1)
        && Objects.equals(integerField2, that.integerField2);
    }

    @Override
    public int hashCode() {
      return Objects.hash(stringField, doubleField, integerField1, integerField2);
    }

    @Override
    public String toString() {
      return "DeserializedTestClass2{" +
        "stringField='" + stringField + '\'' +
        ", doubleField=" + doubleField +
        ", integerField1=" + integerField1 +
        ", integerField2=" + integerField2 +
        '}';
    }
  }

  static class TestFileTransformer1 implements FileTransformer<DeserializedTestClass2> {

    @Override
    public DeserializedTestClass2 transform(InputStream rawDataStream) {
      try {
        var intermediate = CoiObjectMapperFactory.getJsonObjectMapper()
          .readValue(rawDataStream, DeserializedTestClass1.class);

        return new DeserializedTestClass2(
          intermediate.stringField,
          intermediate.doubleField,
          intermediate.mapField.get("integer1"),
          intermediate.mapField.get("integer2")
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
