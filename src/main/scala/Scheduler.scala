import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.MILLISECONDS
import scala.concurrent.duration.TimeUnit
import cats.effect.Clock
import cats.effect.Timer
import cats.effect.Sync
import cats.implicits._

object Scheduler {
    def periodic[F[_] : Sync, A](interval: FiniteDuration, task: F[A])(implicit T: Timer[F]): F[A] = for {
        m <- measure(task)
        (_, elapsed) = m
        remaining = interval - elapsed
        _ <- T.sleep(remaining)
        result <- periodic(interval, task)
    } yield result


    def measure[F[_], A](fa: F[A])(implicit S: Sync[F], C: Clock[F]): F[(A, FiniteDuration)] = for {
        start <- C.monotonic(MILLISECONDS)
        result <- fa
        finish <- C.monotonic(MILLISECONDS)
    } yield (result, FiniteDuration(finish - start, MILLISECONDS))
}
