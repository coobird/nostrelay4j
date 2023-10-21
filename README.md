# nostrelay4j - a Nostr relay written in Java

```text
                     __            __            __ __  _ 
   ____  ____  _____/ /_________  / /___ ___  __/ // / (_)
  / __ \/ __ \/ ___/ __/ ___/ _ \/ / __ `/ / / / // /_/ / 
 / / / / /_/ (__  ) /_/ /  /  __/ / /_/ / /_/ /__  __/ /  
/_/ /_/\____/____/\__/_/   \___/_/\__,_/\__, /  /_/_/ /   
                                       /____/    /___/    
```

nostrelay4j is a Nostr relay written in Java.
It's designed to be extensible with future aspirations to provide a scalable Nostr relay.

_Note: nostrelay4j is not ready for production use._

Supported storage backends:
* Sqlite

## Supported NIPs

* [NIP-1: Basic protocol flow description][1] - Incomplete
* [NIP-11: Relay Information Document][11]

## Requirements

nostrelay4j requires Java 17.

## Installation

TODO.

## Build instructions

To build, run `mvn package`

## TODO List

* Integration tests with a client that calls the server.

[1]: https://github.com/nostr-protocol/nips/blob/master/01.md
[11]: https://github.com/nostr-protocol/nips/blob/master/11.md