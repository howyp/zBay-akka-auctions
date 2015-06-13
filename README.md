# zBay-akka-auctions
[![Build Status](https://travis-ci.org/howyp/zBay-akka-auctions.svg?branch=master)](https://travis-ci.org/howyp/zBay-akka-auctions)

Example of building an auction-hosting solution using Akka. This aims to show many of the great features of Akka for building concurrent systems, whilst remaining realistic in its usage.

## Step 1: Basic Actors - Auction as an actor

Source tag: [ex1_basicActor](tree/ex9_fsm)

We start with one basic actor, which looks after the status of a single auction:

```scala
class Auction extends Actor {
  import Auction.Protocol._

  var currentHighestBid = BigDecimal(0)

  def receive = {
    case StatusRequest => sender ! StatusResponse(currentHighestBid)
    case Bid(value)    => currentHighestBid = currentHighestBid max value
  }
}
```

Its job is to guard a single piece of mutable state, which is the `currentHighestBid`. It accepts two messages:

* `Bid(value)` - submits a bid for the auction. If submitted bid is highest, it replaces the `currentHighestBid`.
* `StatusRequest` - to which the `Auction` replies with the `currentHighestBid`.

## Step 2: Scheduling - Ending auctions

tag: ex2_scheduler

An auction that never finishes is not very useful. Most auctions have a finite finish time, so let's add an `endTime` that is passed to the actor when it is created:

```scala
class Auction(endTime: DateTime) extends Actor {
  import Auction.Protocol._

  var currentHighestBid = BigDecimal(0)
  var currentStatus: State = Running

  context.system.scheduler.scheduleOnce(atEndTime(), self, EndNotification)(context.system.dispatcher)

  def receive = {
    case StatusRequest   => sender ! StatusResponse(currentHighestBid, currentStatus)
    case Bid(value)      => currentHighestBid = currentHighestBid max value
    case EndNotification => currentStatus = Ended
  }

  def atEndTime() = FiniteDuration(new Interval(now, endTime).toDurationMillis, TimeUnit.MILLISECONDS)
}
```

The actor now holds a new mutable state `currentStatus`, which is initialised as `Running`. When it is started up, it calls the scheduler to send itself an `EndNotification` at the appropriate time. Handling this message is very simple - the state of the auction changes to `Ended`.

Note that the scheduler is only actually accurate to a few hundred milliseconds. This can be tuned in the Akka config, but if tight precision on closing time is required, it may be better to keep a list of bids and discount those made after the end.

## Step 3: Behaviours - Ignoring bids after auction ends

tag: ex3_behaviours

A shortcoming of the previous step was that the actor keeps accepting bids after the auction has ended. Let's fix that by dynamically changing the behaviour attached to the `receive` method:

```scala
def receive = runningBehaviour

val runningBehaviour: Receive = {
  case StatusRequest   => sender ! StatusResponse(currentHighestBid, Running)
  case Bid(value)      => currentHighestBid = currentHighestBid max value
  case EndNotification => context.become(endedBehaviour)
}
val endedBehaviour: Receive = {
  case StatusRequest   => sender ! StatusResponse(currentHighestBid, Ended)
}
```

When initialised, we assign the actor the `runningBehaviour`. This is mostly unchanged, except that the `Running` state can always be returned from a status request. 

The major difference is that when we receive an `EndNotification`, we change behaviour to the `endedBehaviour`. This only responds to requests for its status, ignoring bids after the finish time.


## Step 4: ActorRefs - Talking to a user actor

tag: ex4_actorRef

It's probable that we need to keep track of which user has bid on which auctions, so that we can show them to the user. To do this, we add another actor:

```scala
class User extends Actor {
  import User.Protocol._

  var auctions = List[ActorRef]()

  def receive = {
    case BidOnNotification(auction)   => auctions = auction :: auctions
    case ListAuctionsRequest          => sender ! ListAuctionsResponse(auctions)
  }
}
```
Like the `Auction` actor, this actors job is to guard a piece of mutable state, which is a the list of auction actors related to a single user. These are stored as a `List[ActorRef]`.

The actor expects to get `BidOnNotification` messages to tell it that its user bid on an auction; on receipt it adds the reference to the auction actor to its `auctions` list. 

This list of auctions can be queried by sending a `ListAuctionsRequest`, to which the actor responds with the current list. Note that it is totally thread-safe to share this list as it is immutable.