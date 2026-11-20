package tn.esprit.autoloc.service;


public interface IGenericService<T, I>
        extends IReadService<T, I>, IWriteService<T, I>, IDeleteService<I> {
}
