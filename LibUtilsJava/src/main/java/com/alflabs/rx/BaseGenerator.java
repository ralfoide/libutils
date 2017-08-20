package com.alflabs.rx;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A base class for custom publishers/generators that tracks the current attached stream.
 */
public class BaseGenerator<E> implements IGenerator<E>, IAttached<E>, IStateChanged<E> {

    private AtomicReference<IStream<? super E>> mStream = new AtomicReference<>();

    @Null
    public IStream<? super E> getStream() {
        return mStream.get();
    }

    protected void publishOnStream(@Null E event) {
        IStream<? super E> stream = getStream();
        if (stream != null) {
            stream._publishOnStream(event);
        }
    }

    @Override
    public void onAttached(@NonNull IStream<? super E> stream) {
        if (mStream.get() != null) {
            throw new PublisherAttachedException("Publisher is already attached to a stream.");
        }
        mStream.set(stream);
    }

    @Override
    public void onDetached(@NonNull IStream<? super E> stream) {
        mStream.set(null);
    }

    @Override
    public void onStateChanged(@NonNull IStream<? super E> stream, @NonNull State newState) {
        if (newState == State.CLOSED && stream == mStream.get()) {
            mStream.lazySet(null); // for GC
        }
    }
}
