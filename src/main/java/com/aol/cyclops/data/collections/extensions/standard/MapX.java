package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.FluentMapX;
import com.aol.cyclops.types.BiFunctor;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.IterableFilterable;
import com.aol.cyclops.types.OnEmpty;
import com.aol.cyclops.types.OnEmptySwitch;
import com.aol.cyclops.types.stream.CyclopsCollectable;

public interface MapX<K, V> extends Map<K, V>, FluentMapX<K, V>, BiFunctor<K, V>, Functor<V>, IterableFilterable<Tuple2<K, V>>, OnEmpty<Tuple2<K, V>>,
        OnEmptySwitch<Tuple2<K, V>, Map<K, V>>, Publisher<Tuple2<K, V>>, Foldable<Tuple2<K, V>>, CyclopsCollectable<Tuple2<K, V>> {

    static <K, V> Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> defaultCollector() {
        return Collectors.toMap(t -> t.v1, t -> t.v2);
    }

    static <K, V> Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> immutableCollector() {
        return Collectors.collectingAndThen(defaultCollector(), Collections::unmodifiableMap);

    }

    static <K, V> Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> toMapX() {
        return Collectors.collectingAndThen(defaultCollector(), (Map<K, V> d) -> new MapXImpl<K, V>(
                                                                                                    d, defaultCollector()));

    }

    public <K, V> Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> getCollector();

    default ReactiveSeq<Tuple2<K, V>> stream() {
        return ReactiveSeq.fromIterable(this.entrySet())
                          .map(e -> Tuple.tuple(e.getKey(), e.getValue()));
    }

    static <K, V> MapX<K, V> empty() {
        return fromMap(new HashMap<K, V>());
    }

    public static <K, V> MapX<K, V> fromMap(Map<? extends K, ? extends V> it) {
        return fromMap(defaultCollector(), it);
    }

    public static <K, V> MapX<K, V> fromMap(Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> collector, Map<? extends K, ? extends V> it) {
        if (it instanceof MapX)
            return (MapX) it;
        if (it instanceof Map)
            return new MapXImpl<K, V>(
                                      (Map) it, collector);
        return new MapXImpl<K, V>(
                                  StreamUtils.stream(it)
                                             .map(e -> Tuple.tuple(e.getKey(), e.getValue()))
                                             .collect(collector),
                                  collector);
    }

    default MapX<K, V> fromStream(ReactiveSeq<Tuple2<K, V>> stream) {
        return new MapXImpl<>(
                              stream.toMap(t -> t.v1, t -> t.v2), getCollector());
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    default Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.sequence.SequenceMCollectable#collectable()
     */
    @Override
    default Collectable<Tuple2<K, V>> collectable() {
        return stream();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
     */
    default <KR, VR> MapX<KR, VR> flatMap(BiFunction<? super K, ? super V, ? extends MapX<KR, VR>> fn) {

        ReactiveSeq<Tuple2<KR, VR>> s = stream().flatMap(t -> fn.apply(t.v1, t.v2)
                                                                .stream());
        return new MapXImpl<>(
                              s.<KR, VR> toMap(t -> t.v1, t -> t.v2), getCollector());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
     */
    @Override
    default <R> MapX<K, R> map(Function<? super V, ? extends R> fn) {

        ReactiveSeq<Tuple2<K, R>> s = stream().map(t -> t.map2(v -> fn.apply(v)));
        return new MapXImpl<>(
                              s.<K, R> toMap(t -> t.v1, t -> t.v2), getCollector());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.MapX#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> MapX<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2) {
        ReactiveSeq<Tuple2<R1, V>> s1 = stream().map(t -> t.map1(v -> fn1.apply(v)));
        ReactiveSeq<Tuple2<R1, R2>> s2 = s1.map(t -> t.map2(v -> fn2.apply(v)));
        return new MapXImpl<>(
                              s2.<R1, R2> toMap(t -> t.v1, t -> t.v2), getCollector());
    }

    @Override
    int size();

    @Override
    default boolean allMatch(Predicate<? super Tuple2<K, V>> c) {
        return CyclopsCollectable.super.allMatch(c);
    }

    @Override
    default boolean anyMatch(Predicate<? super Tuple2<K, V>> c) {
        return CyclopsCollectable.super.anyMatch(c);
    }

    @Override
    default boolean noneMatch(Predicate<? super Tuple2<K, V>> c) {
        return CyclopsCollectable.super.noneMatch(c);
    }

    @Override
    default Optional<Tuple2<K, V>> max(Comparator<? super Tuple2<K, V>> comparator) {
        return CyclopsCollectable.super.max(comparator);
    }

    @Override
    default Optional<Tuple2<K, V>> min(Comparator<? super Tuple2<K, V>> comparator) {
        return CyclopsCollectable.super.min(comparator);
    }

    @Override
    default MapX<K, V> plus(K key, V value) {
        return (MapX<K, V>) FluentMapX.super.plus(key, value);
    }

    @Override
    default MapX<K, V> plusAll(Map<? extends K, ? extends V> map) {
        return (MapX<K, V>) FluentMapX.super.plusAll(map);
    }

    default MapX<K, V> minus(Object key) {
        return (MapX<K, V>) FluentMapX.super.minus(key);
    }

    default MapX<K, V> minusAll(Collection<?> keys) {
        return (MapX<K, V>) FluentMapX.super.minusAll(keys);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> MapX<K, U> cast(Class<? extends U> type) {

        return (MapX<K, U>) Functor.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default MapX<K, V> peek(Consumer<? super V> c) {

        return (MapX<K, V>) Functor.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> MapX<K, R> trampoline(Function<? super V, ? extends Trampoline<? extends R>> mapper) {

        return (MapX<K, R>) Functor.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    default MapX<K, V> filter(Predicate<? super Tuple2<K, V>> fn) {
        return stream().filter(fn)
                       .toMapX(t -> t.v1, t -> t.v2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default MapX<K, V> filterNot(Predicate<? super Tuple2<K, V>> fn) {

        return (MapX<K, V>) IterableFilterable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
     */
    @Override
    default MapX<K, V> notNull() {

        return (MapX<K, V>) IterableFilterable.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.util.stream.Stream)
     */
    @Override
    default MapX<K, V> removeAll(Stream<? extends Tuple2<K, V>> stream) {

        return (MapX<K, V>) IterableFilterable.super.removeAll(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Iterable)
     */
    @Override
    default MapX<K, V> removeAll(Iterable<? extends Tuple2<K, V>> it) {

        return (MapX<K, V>) IterableFilterable.super.removeAll(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Object[])
     */
    @Override
    default MapX<K, V> removeAll(Tuple2<K, V>... values) {

        return (MapX<K, V>) IterableFilterable.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Iterable)
     */
    @Override
    default MapX<K, V> retainAll(Iterable<? extends Tuple2<K, V>> it) {

        return (MapX<K, V>) IterableFilterable.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.util.stream.Stream)
     */
    @Override
    default MapX<K, V> retainAll(Stream<? extends Tuple2<K, V>> stream) {

        return (MapX<K, V>) IterableFilterable.super.retainAll(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Object[])
     */
    @Override
    default MapX<K, V> retainAll(Tuple2<K, V>... values) {

        return (MapX<K, V>) IterableFilterable.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> MapX<K, R> patternMatch(Function<CheckValue1<V, R>, CheckValue1<V, R>> case1, Supplier<? extends R> otherwise) {

        return (MapX<K, R>) Functor.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.BiFunctor#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default MapX<K, V> bipeek(Consumer<? super K> c1, Consumer<? super V> c2) {

        return (MapX<K, V>) BiFunctor.super.bipeek(c1, c2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.BiFunctor#bicast(java.lang.Class, java.lang.Class)
     */
    @Override
    default <U1, U2> MapX<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {

        return (MapX<U1, U2>) BiFunctor.super.bicast(type1, type2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.BiFunctor#bitrampoline(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> MapX<R1, R2> bitrampoline(Function<? super K, ? extends Trampoline<? extends R1>> mapper1,
            Function<? super V, ? extends Trampoline<? extends R2>> mapper2) {

        return (MapX<R1, R2>) BiFunctor.super.bitrampoline(mapper1, mapper2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(Subscriber<? super Tuple2<K, V>> s) {

        stream().subscribe(s);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmpty#onEmpty(java.lang.Object)
     */
    @Override
    default MapX<K, V> onEmpty(Tuple2<K, V> value) {

        return fromStream(stream().onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmpty#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default MapX<K, V> onEmptyGet(Supplier<? extends Tuple2<K, V>> supplier) {

        return fromStream(stream().onEmptyGet(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmpty#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> MapX<K, V> onEmptyThrow(Supplier<? extends X> supplier) {

        return fromStream(stream().onEmptyThrow(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default MapX<K, V> onEmptySwitch(Supplier<? extends Map<K, V>> supplier) {
        if (this.isEmpty())
            return MapX.fromMap(supplier.get());
        return this;
    }

}
