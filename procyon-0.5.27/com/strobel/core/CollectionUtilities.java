package com.strobel.core;

import com.strobel.functions.*;
import com.strobel.annotations.*;
import java.lang.reflect.*;
import java.util.*;
import com.strobel.util.*;

public final class CollectionUtilities
{
    private static final Supplier IDENTITY_MAP_SUPPLIER;
    private static final Supplier HASH_MAP_SUPPLIER;
    private static final Supplier LINKED_HASH_MAP_SUPPLIER;
    private static final Supplier LIST_SUPPLIER;
    private static final Supplier SET_SUPPLIER;
    
    static {
        IDENTITY_MAP_SUPPLIER = new Supplier() {
            @Override
            public Map get() {
                return new IdentityHashMap();
            }
        };
        HASH_MAP_SUPPLIER = new Supplier() {
            @Override
            public Map get() {
                return new HashMap();
            }
        };
        LINKED_HASH_MAP_SUPPLIER = new Supplier() {
            @Override
            public Map get() {
                return new LinkedHashMap();
            }
        };
        LIST_SUPPLIER = new Supplier() {
            @Override
            public List get() {
                return new ArrayList();
            }
        };
        SET_SUPPLIER = new Supplier() {
            @Override
            public Set get() {
                return new LinkedHashSet();
            }
        };
    }
    
    public static <T> Supplier<Set<T>> setFactory() {
        return CollectionUtilities.SET_SUPPLIER;
    }
    
    public static <T> Supplier<List<T>> listFactory() {
        return CollectionUtilities.LIST_SUPPLIER;
    }
    
    public static <K, V> Supplier<Map<K, V>> hashMapFactory() {
        return CollectionUtilities.HASH_MAP_SUPPLIER;
    }
    
    public static <K, V> Supplier<Map<K, V>> linekdHashMapFactory() {
        return CollectionUtilities.LINKED_HASH_MAP_SUPPLIER;
    }
    
    public static <K, V> Supplier<Map<K, V>> identityMapFactory() {
        return CollectionUtilities.IDENTITY_MAP_SUPPLIER;
    }
    
    public static <T> int indexOfByIdentity(final List<?> collection, final T item) {
        for (int i = 0, n = collection.size(); i < n; ++i) {
            if (collection.get(i) == item) {
                return i;
            }
        }
        return -1;
    }
    
    public static <T> int indexOfByIdentity(final Iterable<?> collection, final T item) {
        VerifyArgument.notNull(collection, "collection");
        if (collection instanceof List) {
            return indexOfByIdentity((List)collection, item);
        }
        int i = -1;
        for (final Object o : collection) {
            ++i;
            if (o == item) {
                return i;
            }
        }
        return -1;
    }
    
    public static <T> int indexOf(final Iterable<? super T> collection, final T item) {
        VerifyArgument.notNull(collection, "collection");
        if (collection instanceof List) {
            return ((List)collection).indexOf(item);
        }
        int i = -1;
        for (final Object o : collection) {
            ++i;
            if (Objects.equals(o, item)) {
                return i;
            }
        }
        return -1;
    }
    
    public static <T> List<T> toList(final Enumeration<T> collection) {
        if (!collection.hasMoreElements()) {
            return Collections.emptyList();
        }
        final ArrayList<T> list = new ArrayList<T>();
        while (collection.hasMoreElements()) {
            list.add(collection.nextElement());
        }
        return list;
    }
    
    public static <T> List<T> toList(final Iterable<T> collection) {
        final ArrayList<T> list = new ArrayList<T>();
        for (final T item : collection) {
            list.add(item);
        }
        return list;
    }
    
    public static <T> T getOrDefault(final Iterable<T> collection, final int index) {
        int i = 0;
        for (final T item : collection) {
            if (i++ == index) {
                return item;
            }
        }
        return null;
    }
    
    public static <T> T getOrDefault(final List<T> collection, final int index) {
        if (index >= VerifyArgument.notNull(collection, "collection").size() || index < 0) {
            return null;
        }
        return collection.get(index);
    }
    
    public static <T> T get(final Iterable<T> collection, final int index) {
        if (VerifyArgument.notNull(collection, "collection") instanceof List) {
            return get((List)collection, index);
        }
        int i = 0;
        for (final T item : collection) {
            if (i++ == index) {
                return item;
            }
        }
        throw Error.indexOutOfRange(index);
    }
    
    public static <T> T get(final List<T> list, final int index) {
        if (index >= VerifyArgument.notNull(list, "list").size() || index < 0) {
            throw Error.indexOutOfRange(index);
        }
        return list.get(index);
    }
    
    public static <T> T single(final List<T> list) {
        switch (VerifyArgument.notNull(list, "list").size()) {
            case 0: {
                throw Error.sequenceHasNoElements();
            }
            case 1: {
                return list.get(0);
            }
            default: {
                throw Error.sequenceHasMultipleElements();
            }
        }
    }
    
    public static <T> T singleOrDefault(final List<T> list) {
        switch (VerifyArgument.notNull(list, "list").size()) {
            case 0: {
                return null;
            }
            case 1: {
                return list.get(0);
            }
            default: {
                throw Error.sequenceHasMultipleElements();
            }
        }
    }
    
    public static <T> T single(final Iterable<T> collection) {
        if (collection instanceof List) {
            return single((List)collection);
        }
        final Iterator<T> it = VerifyArgument.notNull(collection, "collection").iterator();
        if (!it.hasNext()) {
            throw Error.sequenceHasNoElements();
        }
        final T result = it.next();
        if (it.hasNext()) {
            throw Error.sequenceHasMultipleElements();
        }
        return result;
    }
    
    public static <T> T first(final List<T> list) {
        if (VerifyArgument.notNull(list, "list").isEmpty()) {
            throw Error.sequenceHasNoElements();
        }
        return list.get(0);
    }
    
    public static <T> T first(final Iterable<T> collection) {
        if (collection instanceof List) {
            return first((List)collection);
        }
        final Iterator<T> it = VerifyArgument.notNull(collection, "collection").iterator();
        if (it.hasNext()) {
            return it.next();
        }
        throw Error.sequenceHasNoElements();
    }
    
    public static <T> T singleOrDefault(final Iterable<T> collection) {
        if (collection instanceof List) {
            return singleOrDefault((List)collection);
        }
        final Iterator<T> it = VerifyArgument.notNull(collection, "collection").iterator();
        if (!it.hasNext()) {
            return null;
        }
        final T result = it.next();
        if (it.hasNext()) {
            throw Error.sequenceHasMultipleElements();
        }
        return result;
    }
    
    public static <T, R> Iterable<R> ofType(final Iterable<T> collection, final Class<R> type) {
        return (Iterable<R>)new OfTypeIterator((Iterable<T>)VerifyArgument.notNull(collection, "collection"), type);
    }
    
    public static <T> T firstOrDefault(final Iterable<T> collection) {
        final Iterator<T> it = VerifyArgument.notNull(collection, "collection").iterator();
        return it.hasNext() ? it.next() : null;
    }
    
    public static <T> T first(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(predicate, "predicate");
        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                return item;
            }
        }
        throw Error.sequenceHasNoElements();
    }
    
    public static <T> T firstOrDefault(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(predicate, "predicate");
        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }
    
    public static <T> T last(final List<T> list) {
        if (VerifyArgument.notNull(list, "list").isEmpty()) {
            throw Error.sequenceHasNoElements();
        }
        return list.get(list.size() - 1);
    }
    
    public static <T> T last(final Iterable<T> collection) {
        VerifyArgument.notNull(collection, "collection");
        if (collection instanceof List) {
            return last((List)collection);
        }
        final Iterator<T> iterator = collection.iterator();
        final boolean hasAny = iterator.hasNext();
        if (!hasAny) {
            throw Error.sequenceHasNoElements();
        }
        T last;
        do {
            last = iterator.next();
        } while (iterator.hasNext());
        return last;
    }
    
    public static <T> T lastOrDefault(final Iterable<T> collection) {
        VerifyArgument.notNull(collection, "collection");
        if (collection instanceof List) {
            final List<T> list = (List)collection;
            return list.isEmpty() ? null : list.get(list.size() - 1);
        }
        T last = null;
        final Iterator<T> loc_0 = collection.iterator();
        while (loc_0.hasNext()) {
            final T item = last = loc_0.next();
        }
        return last;
    }
    
    public static <T> int firstIndexWhere(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");
        int index = 0;
        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                return index;
            }
            ++index;
        }
        return -1;
    }
    
    public static <T> int lastIndexWhere(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");
        int index = 0;
        int lastMatch = -1;
        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                lastMatch = index;
            }
            ++index;
        }
        return lastMatch;
    }
    
    public static <T> T last(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");
        T lastMatch = null;
        boolean matchFound = false;
        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                lastMatch = item;
                matchFound = true;
            }
        }
        if (matchFound) {
            return lastMatch;
        }
        throw Error.sequenceHasNoElements();
    }
    
    public static <T> T lastOrDefault(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");
        T lastMatch = null;
        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                lastMatch = item;
            }
        }
        return lastMatch;
    }
    
    public static <T> boolean contains(final Iterable<? super T> collection, final T node) {
        if (collection instanceof Collection) {
            return ((Collection)collection).contains(node);
        }
        for (final Object item : collection) {
            if (Comparer.equals(item, node)) {
                return true;
            }
        }
        return false;
    }
    
    public static <T> boolean any(final Iterable<T> collection) {
        if (collection instanceof Collection) {
            return !((Collection)collection).isEmpty();
        }
        return collection != null && collection.iterator().hasNext();
    }
    
    public static <T> Iterable<T> skip(final Iterable<T> collection, final int count) {
        return new SkipIterator<T>(collection, count);
    }
    
    public static <T> Iterable<T> skipWhile(final Iterable<T> collection, final Predicate<? super T> filter) {
        return new SkipIterator<T>(collection, filter);
    }
    
    public static <T> Iterable<T> take(final Iterable<T> collection, final int count) {
        return new TakeIterator<T>(collection, count);
    }
    
    public static <T> Iterable<T> takeWhile(final Iterable<T> collection, final Predicate<? super T> filter) {
        return new TakeIterator<T>(collection, filter);
    }
    
    public static <T> boolean any(final Iterable<T> collection, final Predicate<? super T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");
        for (final T t : collection) {
            if (predicate.test((Object)t)) {
                return true;
            }
        }
        return false;
    }
    
    public static <T> boolean all(final Iterable<T> collection, final Predicate<? super T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");
        for (final T t : collection) {
            if (!predicate.test((Object)t)) {
                return false;
            }
        }
        return true;
    }
    
    public static <T> Iterable<T> where(final Iterable<T> source, final Predicate<? super T> filter) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(filter, "filter");
        if (source instanceof WhereSelectIterableIterator) {
            return ((WhereSelectIterableIterator)source).where(filter);
        }
        return (Iterable<T>)new WhereSelectIterableIterator(source, filter, null);
    }
    
    public static <T, R> Iterable<R> select(final Iterable<T> source, final Selector<? super T, ? extends R> selector) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(selector, "selector");
        if (source instanceof WhereSelectIterableIterator) {
            return ((WhereSelectIterableIterator)source).select(selector);
        }
        return (Iterable<R>)new WhereSelectIterableIterator(source, null, selector);
    }
    
    public static int hashCode(final List<?> sequence) {
        VerifyArgument.notNull(sequence, "sequence");
        int hashCode = 1642088727;
        for (int i = 0; i < sequence.size(); ++i) {
            final Object item = sequence.get(i);
            int itemHashCode;
            if (item instanceof Iterable) {
                itemHashCode = hashCode((Iterable)item);
            }
            else {
                itemHashCode = ((item != null) ? HashUtilities.hashCode(item) : 1642088727);
            }
            hashCode = HashUtilities.combineHashCodes(hashCode, itemHashCode);
        }
        return hashCode;
    }
    
    public static int hashCode(final Iterable<?> sequence) {
        if (sequence instanceof List) {
            return hashCode((List)sequence);
        }
        VerifyArgument.notNull(sequence, "sequence");
        int hashCode = 1642088727;
        for (final Object item : sequence) {
            int itemHashCode;
            if (item instanceof Iterable) {
                itemHashCode = hashCode((Iterable)item);
            }
            else {
                itemHashCode = ((item != null) ? HashUtilities.hashCode(item) : 1642088727);
            }
            hashCode = HashUtilities.combineHashCodes(hashCode, itemHashCode);
        }
        return hashCode;
    }
    
    public static <T> boolean sequenceEquals(final List<? extends T> first, final List<? extends T> second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");
        if (first == second) {
            return true;
        }
        if (first.size() != second.size()) {
            return false;
        }
        if (first.isEmpty()) {
            return true;
        }
        for (int i = 0, n = first.size(); i < n; ++i) {
            if (!Comparer.equals((T)first.get(i), (T)second.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    public static <T> boolean sequenceEquals(final Iterable<? extends T> first, final Iterable<? extends T> second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");
        if (first == second) {
            return true;
        }
        if (first instanceof List && second instanceof List) {
            return sequenceDeepEquals((List)first, (List)second);
        }
        final Iterator<? extends T> firstIterator = first.iterator();
        final Iterator<? extends T> secondIterator = second.iterator();
        while (firstIterator.hasNext()) {
            if (!secondIterator.hasNext()) {
                return false;
            }
            if (!Comparer.equals((T)firstIterator.next(), (T)secondIterator.next())) {
                return false;
            }
        }
        return !secondIterator.hasNext();
    }
    
    public static <T> boolean sequenceDeepEquals(final List<? extends T> first, final List<? extends T> second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");
        if (first == second) {
            return true;
        }
        if (first.size() != second.size()) {
            return false;
        }
        if (first.isEmpty()) {
            return true;
        }
        for (int i = 0, n = first.size(); i < n; ++i) {
            if (!sequenceDeepEqualsCore(first.get(i), second.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    public static <T> boolean sequenceDeepEquals(final Iterable<? extends T> first, final Iterable<? extends T> second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");
        if (first == second) {
            return true;
        }
        if (first instanceof List && second instanceof List) {
            return sequenceDeepEquals((List)first, (List)second);
        }
        final Iterator<? extends T> firstIterator = first.iterator();
        final Iterator<? extends T> secondIterator = second.iterator();
        while (firstIterator.hasNext()) {
            if (!secondIterator.hasNext()) {
                return false;
            }
            if (!sequenceDeepEqualsCore(firstIterator.next(), secondIterator.next())) {
                return false;
            }
        }
        return !secondIterator.hasNext();
    }
    
    private static boolean sequenceDeepEqualsCore(final Object first, final Object second) {
        if (first instanceof List) {
            return second instanceof List && sequenceDeepEquals((List)first, (List)second);
        }
        return Comparer.deepEquals(first, second);
    }
    
    public static <E> E[] toArray(final Class<E> elementType, final Iterable<? extends E> sequence) {
        VerifyArgument.notNull(elementType, "elementType");
        VerifyArgument.notNull(sequence, "sequence");
        return new Buffer<E>(elementType, sequence.iterator()).toArray();
    }
    
    private abstract static class AbstractIterator<T> implements Iterable<T>, Iterator<T>
    {
        static final int STATE_UNINITIALIZED = 0;
        static final int STATE_NEED_NEXT = 1;
        static final int STATE_HAS_NEXT = 2;
        static final int STATE_FINISHED = 3;
        long threadId;
        int state;
        T next;
        
        AbstractIterator() {
            super();
            this.threadId = Thread.currentThread().getId();
        }
        
        @Override
        protected abstract AbstractIterator<T> clone();
        
        @Override
        public abstract boolean hasNext();
        
        @Override
        public T next() {
            if (!this.hasNext()) {
                throw new IllegalStateException();
            }
            this.state = 1;
            return this.next;
        }
        
        @NotNull
        @Override
        public Iterator<T> iterator() {
            if (this.threadId == Thread.currentThread().getId() && this.state == 0) {
                this.state = 1;
                return this;
            }
            final AbstractIterator<T> duplicate = this.clone();
            duplicate.state = 1;
            return duplicate;
        }
        
        @Override
        public final void remove() {
            throw ContractUtils.unsupported();
        }
    }
    
    private static final class OfTypeIterator<T, R> extends AbstractIterator<R>
    {
        final Iterable<T> source;
        final Class<R> type;
        Iterator<T> iterator;
        
        OfTypeIterator(final Iterable<T> source, final Class<R> type) {
            super();
            this.source = VerifyArgument.notNull(source, "source");
            this.type = VerifyArgument.notNull(type, "type");
        }
        
        @Override
        protected OfTypeIterator<T, R> clone() {
            return new OfTypeIterator<T, R>(this.source, this.type);
        }
        
        @Override
        public boolean hasNext() {
            switch (this.state) {
                case 1: {
                    if (this.iterator == null) {
                        this.iterator = this.source.iterator();
                    }
                    while (this.iterator.hasNext()) {
                        final T current = this.iterator.next();
                        if (this.type.isInstance(current)) {
                            this.state = 2;
                            this.next = (T)current;
                            return true;
                        }
                    }
                    this.state = 3;
                }
                case 3: {
                    return false;
                }
                case 2: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
    }
    
    private static final class SkipIterator<T> extends AbstractIterator<T>
    {
        private static final int STATE_NEED_SKIP = 4;
        final Iterable<T> source;
        final int skipCount;
        final Predicate<? super T> skipFilter;
        int skipsRemaining;
        Iterator<T> iterator;
        
        SkipIterator(final Iterable<T> source, final int skipCount) {
            super();
            this.source = VerifyArgument.notNull(source, "source");
            this.skipCount = skipCount;
            this.skipFilter = null;
            this.skipsRemaining = skipCount;
        }
        
        SkipIterator(final Iterable<T> source, final Predicate<? super T> skipFilter) {
            super();
            this.source = VerifyArgument.notNull(source, "source");
            this.skipCount = 0;
            this.skipFilter = VerifyArgument.notNull(skipFilter, "skipFilter");
        }
        
        @Override
        protected SkipIterator<T> clone() {
            if (this.skipFilter != null) {
                return new SkipIterator<T>(this.source, this.skipFilter);
            }
            return new SkipIterator<T>(this.source, this.skipCount);
        }
        
        @Override
        public boolean hasNext() {
            switch (this.state) {
                case 4: {
                    this.iterator = this.source.iterator();
                    if (this.skipFilter != null) {
                        while (this.iterator.hasNext()) {
                            final T current = this.iterator.next();
                            if (!this.skipFilter.test((Object)current)) {
                                this.state = 2;
                                this.next = current;
                                return true;
                            }
                        }
                    }
                    else {
                        while (this.iterator.hasNext() && this.skipsRemaining > 0) {
                            this.iterator.next();
                            --this.skipsRemaining;
                        }
                    }
                    this.state = 1;
                }
                case 1: {
                    if (this.iterator.hasNext()) {
                        this.state = 2;
                        this.next = this.iterator.next();
                        return true;
                    }
                    this.state = 3;
                    return false;
                }
                case 3: {
                    return false;
                }
                case 2: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
        
        @NotNull
        @Override
        public Iterator<T> iterator() {
            if (this.threadId == Thread.currentThread().getId() && this.state == 0) {
                this.state = 4;
                return this;
            }
            final SkipIterator<T> duplicate = this.clone();
            duplicate.state = 4;
            return duplicate;
        }
    }
    
    private static final class TakeIterator<T> extends AbstractIterator<T>
    {
        final Iterable<T> source;
        final int takeCount;
        final Predicate<? super T> takeFilter;
        Iterator<T> iterator;
        int takesRemaining;
        
        TakeIterator(final Iterable<T> source, final int takeCount) {
            super();
            this.source = VerifyArgument.notNull(source, "source");
            this.takeCount = takeCount;
            this.takeFilter = null;
            this.takesRemaining = takeCount;
        }
        
        TakeIterator(final Iterable<T> source, final Predicate<? super T> takeFilter) {
            super();
            this.source = VerifyArgument.notNull(source, "source");
            this.takeCount = Integer.MAX_VALUE;
            this.takeFilter = VerifyArgument.notNull(takeFilter, "takeFilter");
            this.takesRemaining = Integer.MAX_VALUE;
        }
        
        TakeIterator(final Iterable<T> source, final int takeCount, final Predicate<? super T> takeFilter) {
            super();
            this.source = VerifyArgument.notNull(source, "source");
            this.takeCount = takeCount;
            this.takeFilter = takeFilter;
            this.takesRemaining = takeCount;
        }
        
        @Override
        protected TakeIterator<T> clone() {
            return new TakeIterator<T>(this.source, this.takeCount, this.takeFilter);
        }
        
        @Override
        public boolean hasNext() {
            switch (this.state) {
                case 1: {
                    if (this.takesRemaining-- > 0) {
                        if (this.iterator == null) {
                            this.iterator = this.source.iterator();
                        }
                        if (this.iterator.hasNext()) {
                            final T current = this.iterator.next();
                            if (this.takeFilter == null || this.takeFilter.test((Object)current)) {
                                this.state = 2;
                                this.next = current;
                                return true;
                            }
                        }
                    }
                    this.state = 3;
                }
                case 3: {
                    return false;
                }
                case 2: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
    }
    
    private static final class WhereSelectIterableIterator<T, R> extends AbstractIterator<R>
    {
        final Iterable<T> source;
        final Predicate<? super T> filter;
        final Selector<? super T, ? extends R> selector;
        Iterator<T> iterator;
        
        WhereSelectIterableIterator(final Iterable<T> source, final Predicate<? super T> filter, final Selector<? super T, ? extends R> selector) {
            super();
            this.source = VerifyArgument.notNull(source, "source");
            this.filter = filter;
            this.selector = selector;
        }
        
        @Override
        protected WhereSelectIterableIterator<T, R> clone() {
            return new WhereSelectIterableIterator<T, R>(this.source, this.filter, this.selector);
        }
        
        @Override
        public boolean hasNext() {
            switch (this.state) {
                case 1: {
                    if (this.iterator == null) {
                        this.iterator = this.source.iterator();
                    }
                    while (this.iterator.hasNext()) {
                        final T item = this.iterator.next();
                        if (this.filter == null || this.filter.test((Object)item)) {
                            this.next = (T)((this.selector != null) ? this.selector.select((Object)item) : item);
                            this.state = 2;
                            return true;
                        }
                    }
                    this.state = 3;
                }
                case 3: {
                    return false;
                }
                case 2: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
        
        public Iterable<R> where(final Predicate<? super R> filter) {
            if (this.selector != null) {
                return new WhereSelectIterableIterator<Object, R>(this, filter, null);
            }
            return new WhereSelectIterableIterator<Object, R>(this.source, Predicates.and(this.filter, filter), null);
        }
        
        public <R2> Iterable<R2> select(final Selector<? super R, ? extends R2> selector) {
            return (Iterable<R2>)new WhereSelectIterableIterator(this.source, this.filter, (Selector<? super Object, ?>)((this.selector != null) ? Selectors.combine(this.selector, selector) : selector));
        }
    }
    
    private static final class Buffer<E>
    {
        final Class<E> elementType;
        E[] items;
        int count;
        
        Buffer(final Class<E> elementType, final Iterator<? extends E> source) {
            super();
            this.elementType = elementType;
            Object[] items = null;
            int count = 0;
            if (source instanceof Collection) {
                final Collection<E> collection = (Collection)source;
                count = collection.size();
                if (count > 0) {
                    items = (Object[])Array.newInstance(elementType, count);
                    collection.toArray(items);
                }
            }
            else {
                while (source.hasNext()) {
                    final E item = (E)source.next();
                    if (items == null) {
                        items = (Object[])Array.newInstance(elementType, 4);
                    }
                    else if (items.length == count) {
                        items = Arrays.copyOf(items, count * 2);
                    }
                    items[count] = item;
                    ++count;
                }
            }
            this.items = items;
            this.count = count;
        }
        
        E[] toArray() {
            if (this.count == 0) {
                return EmptyArrayCache.fromElementType(this.elementType);
            }
            if (this.items.length == this.count) {
                return (E[])this.items;
            }
            return Arrays.copyOf(this.items, this.count);
        }
    }
}
