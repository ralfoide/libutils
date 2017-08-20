package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A default publisher that just sends events into its stream.
 */
class BasePublisher<E> implements IPublish<E>, IAttached<E> {

    private final Map<IStream<? super E>, Boolean> mStreams = new ConcurrentHashMap<>(1, 0.75f, 1);    // thread-safe

    protected Set<IStream<? super E>> getStreams() {
        return mStreams.keySet();
    }

    @NonNull
    public IPublish<E> publish(@Null E event) {
        for (IStream<? super E> stream : mStreams.keySet()) {
            stream._publishOnStream(event);
        }
        return this;
    }

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {
        mStreams.put(stream, Boolean.TRUE);
    }

    @Override
    public void onDetached(@NonNull IStream<? super E> stream) {
        mStreams.remove(stream);
    }
}
