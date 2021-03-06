package com.aol.cyclops.types.applicative;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.util.function.CurryVariance;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApplyingApplicativeBuilder<T, R, A extends ApplicativeFunctor<R>> {

    private final Unit unit;
    private final Functor functor;

    private ApplicativeFunctor unit(Function fn) {
        return (ApplicativeFunctor) unit.unit(fn);
    }

    public EagerApplicative<T, R, A> applicative(ApplicativeFunctor<Function<? super T, ? extends R>> fn) {

        return () -> fn;
    }

    public EagerApplicative<T, R, A> applicative(Function<? super T, ? extends R> fn) {

        return applicative(unit(fn));
    }

    public <T2> EagerApplicative<T2, R, A> applicative2(ApplicativeFunctor<Function<? super T, Function<? super T2, ? extends R>>> fn) {
        Applicative2<T, T2, R, A> app = () -> fn;
        return app.ap(functor);

    }

    public <T2> EagerApplicative<T2, R, A> applicative2(Function<? super T, Function<? super T2, ? extends R>> fn) {

        return applicative2(unit(fn));
    }

    public <T2> EagerApplicative<T2, R, A> applicative2(BiFunction<? super T, ? super T2, ? extends R> fn) {

        return applicative2(unit(CurryVariance.curry2(fn)));
    }

    public <T2, T3> Applicative2<T2, T3, R, A> applicative3(
            ApplicativeFunctor<Function<? super T, Function<? super T2, Function<? super T3, ? extends R>>>> fn) {
        Applicative3<T, T2, T3, R, A> app = () -> fn;
        return app.ap(functor);
    }

    public <T2, T3> Applicative2<T2, T3, R, A> applicative3(Function<? super T, Function<? super T2, Function<? super T3, ? extends R>>> fn) {

        return applicative3(unit(fn));
    }

    public <T2, T3> Applicative2<T2, T3, R, A> applicative3(TriFunction<? super T, ? super T2, ? super T3, ? extends R> fn) {

        return applicative3(unit(CurryVariance.curry3(fn)));
    }

    public <T2, T3, T4> Applicative3<T2, T3, T4, R, A> applicative4(
            ApplicativeFunctor<Function<? super T, Function<? super T2, Function<? super T3, Function<? super T4, ? extends R>>>>> fn) {
        Applicative4<T, T2, T3, T4, R, A> app = () -> fn;
        return app.ap(functor);
    }

    public <T2, T3, T4> Applicative3<T2, T3, T4, R, A> applicative4(
            Function<? super T, Function<? super T2, Function<? super T3, Function<? super T4, ? extends R>>>> fn) {

        return applicative4(unit(fn));
    }

    public <T2, T3, T4> Applicative3<T2, T3, T4, R, A> applicative4(QuadFunction<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {

        return applicative4(unit(CurryVariance.curry4(fn)));
    }

    public <T2, T3, T4, T5> Applicative4<T2, T3, T4, T5, R, A> applicative5(
            ApplicativeFunctor<Function<? super T, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, ? extends R>>>>>> fn) {
        Applicative5<T, T2, T3, T4, T5, R, A> app = () -> fn;
        return app.ap(functor);
    }

    public <T2, T3, T4, T5> Applicative4<T2, T3, T4, T5, R, A> applicative5(
            Function<? super T, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, ? extends R>>>>> fn) {

        return applicative5(unit(fn));
    }

    public <T2, T3, T4, T5> Applicative4<T2, T3, T4, T5, R, A> applicative5(
            QuintFunction<? super T, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> fn) {

        return applicative5(unit(CurryVariance.curry5(fn)));
    }

}
