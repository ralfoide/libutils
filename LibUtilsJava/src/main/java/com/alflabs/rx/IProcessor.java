package com.alflabs.rx;

import com.alflabs.annotations.Null;

public interface IProcessor<InEvent, OutEvent> extends IStateChanged<InEvent> {
    @Null IStream<OutEvent> output();
    void process(InEvent event);
}
