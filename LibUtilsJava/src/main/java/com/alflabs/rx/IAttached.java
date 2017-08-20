package com.alflabs.rx;

import com.alflabs.annotations.NonNull;

/**
 * Optional decorator interface for {@link ISubscriber}, {@link IGenerator}, and {@link IProcessor}
 * indicating the object wants to be notified when the object is attached (added) or detached (removed)
 * from a stream.
 */
public interface IAttached<Event> {
    /**
     * The {@link ISubscriber}, {@link IGenerator}, or {@link IProcessor} was attached (added) to the stream.
     *
     * @throws PublisherAttachedException if publisher is already attached.
     */
    void onAttached(@NonNull IStream<? super Event> stream);

    /**
     * The {@link ISubscriber}, {@link IGenerator}, or {@link IProcessor} was detached (removed) from the stream.
     */
    void onDetached(@NonNull IStream<? super Event> stream);
}
