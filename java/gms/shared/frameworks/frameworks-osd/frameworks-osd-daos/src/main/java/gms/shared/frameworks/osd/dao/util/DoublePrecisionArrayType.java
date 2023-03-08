package gms.shared.frameworks.osd.dao.util;

import com.vladmihalcea.hibernate.type.array.internal.ArraySqlTypeDescriptor;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.usertype.DynamicParameterizedType;

import java.util.Properties;

public class DoublePrecisionArrayType extends AbstractSingleColumnStandardBasicType<double[]>
  implements DynamicParameterizedType {

  public DoublePrecisionArrayType() {
    super(ArraySqlTypeDescriptor.INSTANCE, DoublePrecisionArrayTypeDescriptor.INSTANCE);
  }

  @Override
  public String getName() {
    return "double-precision-array";
  }

  @Override
  protected boolean registerUnderJavaType() {
    return true;
  }

  @Override
  public void setParameterValues(Properties parameters) {
    ((DoublePrecisionArrayTypeDescriptor) getJavaTypeDescriptor()).setParameterValues(parameters);
  }
}
