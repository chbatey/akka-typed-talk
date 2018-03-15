package info.batey.akka

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl._
import info.batey.akka.TypedLockProtocol._
import TypedActors._
import akka.actor.Actor
import akka.actor.typed.receptionist.Receptionist.{Find, Register}
import akka.actor.typed.receptionist.ServiceKey
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object TypedLockProtocol {
  sealed trait LockProtocol
  final case class Lock(self: ActorRef[LockStatus]) extends LockProtocol
  final case class Unlock(self: ActorRef[LockStatus]) extends LockProtocol

  sealed trait LockStatus
  final case object Granted extends LockStatus
  final case class Taken(who: ActorRef[LockStatus]) extends LockStatus
  final case object Released extends LockStatus
  final case object UnlockFailed extends LockStatus
}

object TypedActors {

  def locked(by: ActorRef[LockStatus]): Behavior[LockProtocol] = Behaviors.immutable[LockProtocol] {
    case (ctx, Lock(who)) =>
      who ! Taken(by)
      Behaviors.same
    case (ctx, Unlock(who)) =>
      require(who == by)
      who ! Released
      unlocked

  }

  val unlocked: Behavior[LockProtocol] = Behaviors.immutable[LockProtocol] {
    case (ctx, Lock(who)) =>
      who ! Granted
      locked(who)
  }

}

object Example extends App {
  implicit val timeout = Timeout(1.second)

  val topLevel = Behaviors.setup[LockStatus] { initialCtx =>
    val lock = initialCtx.spawn(unlocked, "lock-a")
    lock ! Lock(initialCtx.self)

    Behaviors.immutable {
      case (ctx, Granted) =>
        ctx.log.info("Yay I have the Lock")

        ctx.ask(lock)(Unlock) {
          case Success(m) => m
          case Failure(t) => UnlockFailed
        }

        Behaviors.same
      case (ctx, Taken(who)) =>
        ctx.log.info("Who date take my lock???")
        Behaviors.same
      case (ctx, Released) =>
        ctx.log.info("No more lock for me :(")
        Behaviors.same
    }
  }

  val system = ActorSystem(topLevel, "TopLevel")
}

object ReceptionistExample extends App {
  implicit val timeout = Timeout(1.second)

  sealed trait NeedsLock
  final case class LockActorAvailable(lock: ActorRef[LockProtocol]) extends NeedsLock
  final case object LockNotAvailable extends NeedsLock
  final case object LockGranted extends NeedsLock

  val needsLockGrant = Behaviors.immutable[NeedsLock] {
    case (ctx, LockGranted) =>
      ctx.log.info("Yay lock, but what do I do with it??")
      Behaviors.same
    case (ctx, LockNotAvailable) =>
      ctx.log.info("Sad panda")
      Behaviors.stopped
  }

  val needsLockInstance = Behaviors.setup[NeedsLock] { initialCtx =>
    val lockKey = ServiceKey[LockProtocol]("lock-a")
    initialCtx.ask(initialCtx.system.receptionist)(Find(lockKey)) {
      case Success(listing) if listing.serviceInstances(lockKey).size == 1 =>
        LockActorAvailable(listing.serviceInstances(lockKey).head)
      case _ =>
        LockNotAvailable
    }

    Behaviors.immutable[NeedsLock] {
      case (ctx, LockActorAvailable(lockActor)) =>
        ctx.log.info("Lock actor is available, time to get it")
        ctx.ask(lockActor)(Lock) {
          case Success(l) => LockGranted
          case Failure(t) => LockNotAvailable
        }
        needsLockGrant
      case (ctx, LockNotAvailable) =>
        ctx.log.info("Oh noes, no lock actor")
        Behaviors.stopped
    }
  }


  val topLevel = Behaviors.setup[NeedsLock] { initialCtx =>
    val lock = initialCtx.spawn(unlocked, "lock-a")
    initialCtx.system.receptionist ! Register(ServiceKey[LockProtocol]("lock-a"), lock)
    needsLockInstance
  }

  val system = ActorSystem(topLevel, "TopLevel")
}
