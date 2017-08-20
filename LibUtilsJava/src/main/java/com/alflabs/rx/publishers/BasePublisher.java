package com.alflabs.rx.publishers;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.rx.IAttached;
import com.alflabs.rx.IPublish;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStream;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A default publisher that just sends event into its stream.
 */
class BasePublisher<E> implements IPublish<E>, IPublisher<E>, IAttached<E> {

    private final Map<IStream<? super E>, Boolean> mStreams = new ConcurrentHashMap<>(1, 0.75f, 1);    // thread-safe

    protected Set<IStream<? super E>> getStreams() {
        return mStreams.keySet();
    }

    @NonNull
    public IPublish<E> publish(@Null E event) {
        for (IStream<? super E> stream : mStreams.keySet()) {
            publishOnStream(event, stream);
        }
        return this;
    }

    void publishOnStream(@Null E event, IStream<? super E> stream) {
        //noinspection unchecked
        ((IPublish) stream).publish(event);
    }

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {
        if (stream instanceof IPublish){
            mStreams.put(stream, Boolean.TRUE);
        }
    }

    @Override
    public void onDetached(@NonNull IStream<? super E> stream) {
        mStreams.remove(stream);
    }
}
