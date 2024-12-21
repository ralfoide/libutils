# LibUtils Mini-RX-Java

## What is it

__LibUtils RX Java__ is a simplified, lightweight implementation of
__Reactive Streams__. It is not RxJava compatible. Exists both for
Java projects and Android projects.

## Rationale

If you are not familiar with the concept of __Reactive Streams__,
consider reading https://reactivex.io to get an introduction on the concept,
and https://github.com/ReactiveX/RxJava to understand the popular RxJava
implementation.

RxJava is fairly large library (130 classes in about a dozen packages)
and it is quite comprehensive, which is why it is a popular choice for many large projects.

By comparison, __LibUtils RX Java__ is a much simpler library with only a handful of files,
and importantly with a much narrower scope. __LibUtils RX Java__ does not try to be comprehensive.

__LibUtils RX Java__ main and only goal is to serve the simple concept of a publisher/subscriber
stream. Exactly one type of publisher is provided, one type of stream, and one type of subscriber.


## Usage

Concept wise:
 - A stream is a typed event bus.
 - Publisher are attached to the stream and send events.
 - Subscribers are attached to the stream and receive events.
 - Schedulers are provided to be able to publish/subscribe from different threads.

The API is best learnt by reading the StreamsTest test file.

A typical example to communicate between e.g. a background thread and an Android activity:

    // Create a stream.
    private final IStream<MyType> mStream = Streams.stream();

    // On the sending side, create a publisher
    private final IPublisher<MyType> mPublisher = Publishers.publisher();
    // attach it to the stream
    mStream.on(Schedulers.sync()).publishWith(mPublisher);
    // and send some data (e.g. UI events, network events)
    mPublisher.publish(new MyType(something1));
    mPublisher.publish(new MyType(something2));

    // On the receiver side, create a subscriber
    mStream.subscribe(AndroidSchedulers.mainThread(), this::onStreamEvent);
    private void onStreamEvent(IStream<? extends MyType> stream, MyType data) {
        // do something with data
    }

In an Android app one could use Dagger (https://dagger.dev/) to provide the stream as a
scoped singleton between the sender and the receiver.

Publishers and subscribers can be ephemeral. On Android, one would
expect them to come and go based on activity or fragment lifecycles.

The Streams class has a single creator method: stream() to create a new stream.

The Subscribers class has a few creator methods: publisher(), latest(), and just(fixed data).

The Schedulers class has sync() for direct calls, io() for background, and there's
AndroidSchedulers.mainThread() for Android. This makes it trivial to publish from an IO thread
and subscribe from the UI thread.

Besides basic publishers, one can also attach generators and processors to a stream.
The difference between a publisher and a generator is mostly semantic: a publisher is an
object that an app will call to publish (via explicit calls to myPublisher.publish(some data)),
whereas a generator is a class that just internally publishes data to its stream.
A processor processes data from an input stream and sends it to an output stream.


## Distribution

__LibUtils RX Java__ is bundled as part of the
[LibUtils project](https://bitbucket.org/ralfoide/libutils).


## License

LibUtils is licensed under the __GNU GPL v3 license__.
The full GPL license is available in the file "LICENSE-gpl-3.0.txt".
Copyright (C) 2017 alf.labs gmail com.
