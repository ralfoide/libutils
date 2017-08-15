package com.alflabs.rx;

import com.alflabs.annotations.Null;

/**
 * A processor transform events from an input stream into events for an output stream.
 * <p/>
 * Processors are typically filters, generators, or transformers (aka "map" in the functional terminology).
 * <p/>
 * Processors subscribe to one or more input streams (to receive events) and publish on an output stream.
 * Processors can have more than one input stream (e.g. a merge/combine processor).
 * <p/>
 * Optional interface: <br/>
 * - if the processor implements {@link IStateChanged<InEvent>}, it will be notified when an input stream changes state. <br/>
 * - if the processor implements {@link IAttached<InEvent>}, it will be notified when attached to an input stream. <br/>
 */
public interface IProcessor<InEvent, OutEvent> {
    @Null IStream<OutEvent> output();
    void process(InEvent event);
}
