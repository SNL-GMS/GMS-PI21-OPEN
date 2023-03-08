package gms.shared.frameworks.osd.dao.util;

import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayTypeDescriptor;

public class DoublePrecisionArrayTypeDescriptor extends AbstractArrayTypeDescriptor<double[]> {

  public static final DoublePrecisionArrayTypeDescriptor INSTANCE =
    new DoublePrecisionArrayTypeDescriptor();

  public DoublePrecisionArrayTypeDescriptor() {
    super(double[].class);
  }

  @Override
  protected String getSqlArrayType() {
    return "float8";
  }
}
