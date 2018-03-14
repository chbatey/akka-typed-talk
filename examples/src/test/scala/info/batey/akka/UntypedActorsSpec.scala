package info.batey.akka

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpec, WordSpecLike}
import LockProtocol._

class UntypedActorsSpec extends TestKit(ActorSystem("UntypedActorsSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An untyped actor" must {
    "get a lock that is free" in {
      val lock = system.actorOf(Props[MutableActor], "lock-a")
      lock ! Lock
      expectMsgType[Granted.type]
    }

    "fail to get a lock that is taken" in {
      val lock = system.actorOf(Props[MutableActor], "lock-b")
      lock ! Lock
      expectMsgType[Granted.type]

      val nextInLine = TestProbe()
      lock.tell(Lock, nextInLine.ref)
      nextInLine.expectMsg(Taken(testActor))
    }
  }

}
