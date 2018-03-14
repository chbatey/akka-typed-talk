package info.batey.akka

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSystem, Props}
import LockProtocol._

object UntypedActors {
  val system = ActorSystem()
  system.actorOf(Props[MutableActor], "chris-lock")
}



class MutableActor extends Actor with ActorLogging {
  import LockProtocol._

  private var owner: Option[ActorRef] = None

  def receive: Receive = {
    case Lock if owner.isEmpty =>
      owner = Some(sender())
      sender() ! Granted
    case Lock =>
      sender() ! Taken(owner.get)
    case Unlock =>
      require(owner.contains(sender()))
      owner = None
  }
}

class BecomeActor extends Actor {
  import LockProtocol._

  private val unlocked: Receive = {
    case Lock =>
      sender() ! Granted
      context.become(locked(sender()))
  }

  private def locked(who: ActorRef): Receive = {
    case Lock =>
      sender() ! Taken(who)
    case Unlock =>
      require(sender() == who)
      context.become(unlocked)
  }


  override def receive = unlocked
}

