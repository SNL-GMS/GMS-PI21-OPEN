package gms.shared.stationdefinition.accessor;

import gms.shared.stationdefinition.api.util.Request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractAccessor<D> {

  protected <R extends Request> List<D> getRequestData(R request,
    Function<R, Collection<D>> cacheLookupFunction,
    Function<R, List<D>> repositoryFunction) {
    final Collection<D> cachedData = cacheLookupFunction.apply(request);
    if (!cachedData.isEmpty()) {
      return new ArrayList<>(cachedData);
    } else {
      final List<D> dataFromRepository = repositoryFunction.apply(request);

      return dataFromRepository;
    }
  }

}
