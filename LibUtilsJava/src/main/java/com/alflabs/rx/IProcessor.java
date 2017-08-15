package com.alflabs.rx;

import com.alflabs.annotations.Null;

/**
 *
 *
 * <p/>
 * Optional interface: <br/>
 * - if the subscriber implements {@link IStateChanged<InEvent>}, it will be notified when the input stream changes state. <br/>
 * - if the subscriber implements {@link IAttached<InEvent>}, it will be notified when attached to the input stream. <br/>
 */
public interface IProcessor<InEvent, OutEvent> {
    @Null IStream<OutEvent> output();
    void process(InEvent event);
}
