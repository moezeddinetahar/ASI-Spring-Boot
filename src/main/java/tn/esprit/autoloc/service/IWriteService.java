package tn.esprit.autoloc.service;

public interface IWriteService<T, I> {
    boolean check(T entity, boolean isNew);
    T create(T entity);
    T update(I id, T entity);
}
