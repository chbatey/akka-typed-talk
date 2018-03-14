package info.batey.akka

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl._
import info.batey.akka.TypedLockProtocol._
import TypedActors._

object TypedLockProtocol {
  sealed trait LockProtocol
  final case class Lock(self: ActorRef[LockStatus]) extends LockProtocol
  final case class Unlock(self: ActorRef[LockStatus]) extends LockProtocol

  sealed trait LockStatus
  final case object Granted extends LockStatus
  final case class Taken(who: ActorRef[LockStatus]) extends LockStatus
}

object TypedActors {

  def locked(by: ActorRef[LockStatus]): Behavior[LockProtocol] = Behaviors.immutable[LockProtocol] {
    case (ctx, Lock(who)) =>
      who ! Taken(by)
      Behaviors.same
    case (ctx, Unlock(who)) =>
      require(who == by)
      Behaviors.same
  }

  val unlocked: Behavior[LockProtocol] = Behaviors.immutable[LockProtocol] {
    case (ctx, Lock(who)) =>
      who ! Granted
      locked(who)
  }

}

object Example extends App {
  val topLevel = Behaviors.setup[LockStatus] { initialCtx =>
    val lock = initialCtx.spawn(unlocked, "lock-a")
    lock ! Lock(initialCtx.self)

    Behaviors.immutable {
      case (ctx, Granted) =>
        ctx.log.info("Yay I have the Lock")
        Behaviors.same
      case (ctx, Taken(who)) =>
        ctx.log.info("Who date take my lock???")
        Behaviors.same
    }
  }

  val system = ActorSystem(topLevel, "TopLevel")
}
