package gms.shared.metrics;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class MetricRegister {
  private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

  public static void register(CustomMetric customMetric, ObjectName name) throws JMException {
    mBeanServer.registerMBean(customMetric, name);
  }
}
