package com.alflabs.rx;

import com.alflabs.annotations.Null;

/**
 *
 *
 * <p/>
 * Optional interface: if the publisher implements {@link IStateChanged<InEvent>}, it will be notified
 * when an input stream changes state.
 */
public interface IProcessor<InEvent, OutEvent> {
    @Null IStream<OutEvent> output();
    void process(InEvent event);
}
