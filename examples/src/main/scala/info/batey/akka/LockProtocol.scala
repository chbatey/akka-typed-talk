package info.batey.akka

import akka.actor.ActorRef

object LockProtocol {
  sealed trait LockProtocol
  final case object Lock extends LockProtocol
  final case object Unlock extends LockProtocol

  sealed trait LockStatus
  final case object Granted extends LockStatus
  final case class Taken(who: ActorRef) extends LockStatus
}
