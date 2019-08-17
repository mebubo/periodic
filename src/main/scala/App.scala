import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.MILLISECONDS
import scala.concurrent.duration.SECONDS
import scala.concurrent.duration.TimeUnit
import cats.effect.Clock
import cats.effect.Timer
import cats.effect.Sync
import cats.implicits._
import cats.effect.Concurrent

object App {
    def print[F[_]](s: String)(implicit S: Sync[F]): F[Unit] = S.delay(println(s))
    def read[F[_]](implicit S: Sync[F]): F[String] = S.delay(readLine)

    def app[F[_]: Sync : Timer](implicit C: Concurrent[F]): F[Unit] = {
        val duration = FiniteDuration(1, SECONDS)
        val periodic = Scheduler.periodic[F, Unit](duration, print("hello"))
        val fiber = C.start(periodic)
        for {
            token <- fiber
            s <- read
            _ <- token.cancel
            _ <- print("stop")

        } yield ()
    }
}