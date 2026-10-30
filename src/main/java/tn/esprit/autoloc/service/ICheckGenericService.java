package tn.esprit.autoloc.service;

import java.util.List;

public interface ICheckGenericService<T,ID> {
    boolean check(T entity, boolean isNew);
}
