import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.MILLISECONDS
import scala.concurrent.duration.SECONDS
import scala.concurrent.duration.TimeUnit
import cats.effect.Clock
import cats.effect.Timer
import cats.effect.Sync
import cats.implicits._
import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.MonadError

object App {
    val duration = FiniteDuration(1, SECONDS)

    def print[F[_]](s: String)(implicit S: Sync[F]): F[Unit] = S.delay(println(s))
    def read[F[_]](implicit S: Sync[F]): F[String] = S.delay(readLine)

    def printingAndCounting[F[_] : Sync : Timer](implicit E: MonadError[F, Throwable]): F[Unit] = for {
        ref <- Ref.of[F, Int](0)
        p = for {
            i <- ref.get
            _ <- if (i == 5) E.raiseError(new Exception("fail")) else print(s"hello $i")
            _ <- ref.modify(x => (x + 1, x))
        } yield ()
        _ <- Scheduler.periodic(duration, p)
    } yield ()

    def printing[F[_] : Sync : Timer]: F[Unit] = Scheduler.periodic(duration, print("hello"))

    def app[F[_]: Sync : Timer](implicit C: Concurrent[F]): F[Unit] = {
        for {
            token <- C.start(printingAndCounting)
            _ <- read
            _ <- token.cancel
            _ <- print("stop")
        } yield ()
    }
}