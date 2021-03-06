package com.aol.cyclops.types.mixins;

import java.util.List;
import java.util.stream.Collectors;

import com.aol.cyclops.internal.invokedynamic.ReflectionCache;
import com.aol.cyclops.util.ExceptionSoftener;

public interface TupleWrapper {

    public Object getInstance();

    @SuppressWarnings("unchecked")
    default List<Object> values() {

        try {
            return (List) ReflectionCache.getFields(getInstance().getClass())
                                         .stream()
                                         .map(f -> {
                                             try {

                                                 return f.get(getInstance());
                                             } catch (Exception e) {
                                                 ExceptionSoftener.throwSoftenedException(e);
                                                 return null;
                                             }
                                         })
                                         .collect(Collectors.toList());
        } catch (Exception e) {
            throw ExceptionSoftener.throwSoftenedException(e);

        }
    }
}