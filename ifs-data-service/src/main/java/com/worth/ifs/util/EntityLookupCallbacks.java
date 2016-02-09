package com.worth.ifs.util;

import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.worth.ifs.commons.error.Errors.internalServerErrorError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static java.util.Optional.ofNullable;

/**
 * Utility class to provide common use case wrappers that can be used to wrap callbacks that require either an entity or
 * some failure message if that entity cannot be found.
 */
public class EntityLookupCallbacks {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(EntityLookupCallbacks.class);

    public static <SuccessType> ServiceResult<SuccessType> find(
            Supplier<SuccessType> getterFn,
            Error failureResponse) {

        SuccessType getterResult = getterFn.get();

        if (getterResult instanceof Collection && ((Collection) getterResult).isEmpty()) {
            return serviceFailure(failureResponse);
        }

        return ofNullable(getterResult).map(ServiceResult::serviceSuccess).orElse(serviceFailure(failureResponse));
    }

    /**
     * This find() method, given 2 ServiceResult suppliers, supplies a ServiceResultTuple2Handler that is able to execute
     * the ServiceResults in a chain and fail early if necessary.  Assuming that they are all successes, a supplied
     * BiFunction can then be called with the 2 successful ServiceResult values as its 2 inputs
     */
    public static <FinalSuccessType, SuccessType1> ServiceResultHandler<SuccessType1> find(
            Supplier<ServiceResult<SuccessType1>> getterFn) {

        return new ServiceResultHandler<>(getterFn);
    }

    /**
     * This find() method, given 2 ServiceResult suppliers, supplies a ServiceResultTuple2Handler that is able to execute
     * the ServiceResults in a chain and fail early if necessary.  Assuming that they are all successes, a supplied
     * BiFunction can then be called with the 2 successful ServiceResult values as its 2 inputs
     */
    public static <FinalSuccessType, SuccessType1, SuccessType2> ServiceResultTuple2Handler<SuccessType1, SuccessType2> find(
            Supplier<ServiceResult<SuccessType1>> getterFn1,
            Supplier<ServiceResult<SuccessType2>> getterFn2) {

        return new ServiceResultTuple2Handler<>(getterFn1, getterFn2);
    }

    /**
     * This find() method, given 2 ServiceResult suppliers, supplies a ServiceResultTuple2Handler that is able to execute
     * the ServiceResults in a chain and fail early if necessary.  Assuming that they are all successes, a supplied
     * BiFunction can then be called with the 2 successful ServiceResult values as its 2 inputs
     */
    public static <FinalSuccessType, SuccessType1, SuccessType2, SuccessType3> ServiceResultTuple3Handler<SuccessType1, SuccessType2, SuccessType3> find(
            Supplier<ServiceResult<SuccessType1>> getterFn1,
            Supplier<ServiceResult<SuccessType2>> getterFn2,
            Supplier<ServiceResult<SuccessType3>> getterFn3) {

        return new ServiceResultTuple3Handler<>(getterFn1, getterFn2, getterFn3);
    }

    public static <T> ServiceResult<T> getOnlyElementOrFail(Collection<T> list) {
        if (list == null || list.size() != 1) {
            return serviceFailure(internalServerErrorError("Found multiple entries in list but expected only 1 - " + list));
        }
        return serviceSuccess(list.iterator().next());
    }

    /**
     * This class is produced by the find() method, which given 2 ServiceResult suppliers, is able to execute the ServiceResults
     * in a chain and fail early if necessary.  Assuming that they are all successes, a supplied BiFunction can then be called
     * with the 2 successful ServiceResult values as its 2 inputs
     *
     * @param <T>
     */
    public static class ServiceResultHandler<T> {

        private Supplier<ServiceResult<T>> getterFn1;

        public ServiceResultHandler(Supplier<ServiceResult<T>> getterFn1) {
            this.getterFn1 = getterFn1;
        }

        public <R> ServiceResult<R> andOnSuccess(Function<T, ServiceResult<R>> mainFunction) {
            return getterFn1.get().andOnSuccess(result1 -> mainFunction.apply(result1));
        }
    }

    /**
     * This class is produced by the find() method, which given 2 ServiceResult suppliers, is able to execute the ServiceResults
     * in a chain and fail early if necessary.  Assuming that they are all successes, a supplied BiFunction can then be called
     * with the 2 successful ServiceResult values as its 2 inputs
     *
     * @param <R>
     * @param <S>
     */
    public static class ServiceResultTuple2Handler<R, S> {

        private Supplier<ServiceResult<R>> getterFn1;
        private Supplier<ServiceResult<S>> getterFn2;

        public ServiceResultTuple2Handler(Supplier<ServiceResult<R>> getterFn1, Supplier<ServiceResult<S>> getterFn2) {
            this.getterFn1 = getterFn1;
            this.getterFn2 = getterFn2;
        }

        public <T> ServiceResult<T> andOnSuccess(BiFunction<R, S, ServiceResult<T>> mainFunction) {
            return getterFn1.get().andOnSuccess(result1 -> getterFn2.get().andOnSuccess(result2 -> mainFunction.apply(result1, result2)));
        }
    }

    /**
     * This class is produced by the find() method, which given 2 ServiceResult suppliers, is able to execute the ServiceResults
     * in a chain and fail early if necessary.  Assuming that they are all successes, a supplied BiFunction can then be called
     * with the 2 successful ServiceResult values as its 2 inputs
     *
     * @param <R>
     * @param <S>
     */
    public static class ServiceResultTuple3Handler<R, S, T> {

        private Supplier<ServiceResult<R>> getterFn1;
        private Supplier<ServiceResult<S>> getterFn2;
        private Supplier<ServiceResult<T>> getterFn3;

        public ServiceResultTuple3Handler(Supplier<ServiceResult<R>> getterFn1, Supplier<ServiceResult<S>> getterFn2, Supplier<ServiceResult<T>> getterFn3) {
            this.getterFn1 = getterFn1;
            this.getterFn2 = getterFn2;
            this.getterFn3 = getterFn3;
        }

        public <A> ServiceResult<A> andOnSuccess(TriFunction<R, S, T, ServiceResult<A>> mainFunction) {
            return getterFn1.get().
                    andOnSuccess(result1 -> getterFn2.get().
                            andOnSuccess(result2 -> getterFn3.get().
                                    andOnSuccess(result3 -> mainFunction.apply(result1, result2, result3))));
        }
    }

    @FunctionalInterface
    public interface TriFunction<R, S, T, A> {

        A apply(R r, S s, T t);
    }
}