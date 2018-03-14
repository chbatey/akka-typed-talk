import akka.actor.ActorSystem
import akka.cluster.Cluster
import org.slf4j.{Logger, LoggerFactory}

object ClusterHelpers {
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def clusterJoined(system: ActorSystem): Future[Unit] = {
    val p = Promise[Unit]()
    Cluster(system).registerOnMemberUp {
      p.success(())
    }

    p.future
  }

  def startCluster(implicit system: ActorSystem): Cluster = {
    implicit val cluster = Cluster(system)
    cluster
  }
}

object BootMain extends App {

  val log = LoggerFactory.getLogger(getClass)
  val config = ConfigFactory.load()

  log.info("using config {}", config)

  val systemName = "Who"

  implicit val system = ActorSystem("Who", config)

  val cluster = ClusterHelpers.startCluster
  

  val clientRef: ActorRef =
    system.actorOf(
      FromConfig
        .withSupervisorStrategy(OneForOneStrategy() {
          case ex => log.warn("router exception: {}", ex); Escalate
        })
        .props(Props(new ClientActor)),
      "client"
    )

  val region: ActorRef =
    ClusterSharding(system).start(
      "Supervisor",
      Props(new Supervisor(clientRef)),
      ClusterShardingSettings(system).withRememberEntities(true),
      Supervisor.idExtractor,
      Supervisor.shardResolver
    )
}
