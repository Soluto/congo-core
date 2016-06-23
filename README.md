# Congo

Congo is an RPC/IPC library that is build on top of Rx. It enables two parties to tranport a stream of messages over various communication layers.
Congo is cross platform - currently implemened in javacript and Java, and is expected to have Swift and C# implementation in the near future.

### Examples
Congo is easy to setup and use. The easiest way to understand it is by looking at the examples at https://github.com/Soluto/congo-examples

## Usage
Java - [congo-core-java](https://github.com/Soluto/congo-core/tree/master/congo-core-java)

### Pluggable Transportation Layer
Communication is implemened as a plugin. You can transport messages with websocket, socket.io, TCP, pubnub etc. You can also use IPC by implementing proper trasport layer (such as intents in android)

For some plugins example see:
 - https://github.com/Soluto/congo-pubnub
 - https://github.com/Soluto/congo-react-native
 - https://github.com/Soluto/congo-android-webview
 - https://github.com/Soluto/congo-android-intent

### Experimental
We are using an earlier version of Congo in production, and would be happy to get some feedback. The project is still experimental and we will put more effort into it in the near future
