package com.alflabs.rx;

public interface IProcessor<InEvent, OutEvent> extends IStateChanged<InEvent> {
    IStream<OutEvent> output();
    void process(InEvent event);
}
