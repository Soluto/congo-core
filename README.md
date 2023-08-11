## DEPRECATED
This repository is no longer maintained and has been archived. Feel free to browse the code, but please migrate to other solutions.

# Congo

Congo is a RPC/IPC library that is build on top of [Reactive Extensions](http://reactivex.io/) 

It enables two parties to transport a stream of messages over various communication layers.

Congo is cross platform - currently implemened in javacript and Java - and is expected to have Swift and C# implementation in the near future.

For simple Java explanation about Congo architecure see [Congo Core Explained](https://github.com/Soluto/congo-core/tree/master/congo-core-java)

### Examples
Congo is easy to setup and use. The easiest way to understand it is by looking at the examples at https://github.com/Soluto/congo-examples

### Pluggable Transportation Layer
Communication is implemened as a plugin. You can transport messages in any communication technology that fits to your needs, and it is easy to implement and use new communication layers.

For some plugins examples see:
 - https://github.com/Soluto/congo-pubnub
 - https://github.com/Soluto/congo-react-native

### Experimental
We are using an earlier version of Congo in production, and would be happy to get some feedback. The project is still experimental and we will put more effort into it in the near future.

###  Contributing
Thanks for thinking about contributing! We are looking for contributions of any sort and size - features, bug fixes, documentation or anything else that you think will make Congo better.

- Fork and clone locally
- Create a topic specific branch
- Add a cool feature or fix a bug
- Add tests
- Send a Pull Request
