package f

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

    def print[F[_] : Sync](s: String): F[Unit] = Sync[F].delay(println(s))
    def read[F[_] : Sync]: F[String] = Sync[F].delay(readLine)

    def doingAndCounting[F[_] : Sync : Timer, A](f: Int => F[A])(implicit E: MonadError[F, Throwable]): F[Unit] = for {
        ref <- Ref.of[F, Int](0)
        p = for {
            i <- ref.get
            _ <- f(i)
            _ <- ref.modify(x => (x + 1, x))
        } yield ()
        _ <- Scheduler.periodic(duration, p)
    } yield ()

    def doing[F[_] : Sync : Timer, A](fa: F[A]): F[Unit] = Scheduler.periodic(duration, fa).map(_ => ())

    def app[F[_]: Sync : Timer : Concurrent : MonadError[*[_], Throwable]]: F[Unit] = {
        val printOrError: Int => F[Unit] = i => if (i == 5) MonadError[F, Throwable].raiseError(new Exception("fail")) else print(s"hello $i")
        for {
            // token <- Concurrent[F].start(doing(print("hello")))
            token <- Concurrent[F].start(doingAndCounting(printOrError))
            _ <- read
            _ <- token.cancel
            _ <- print("stop")
        } yield ()
    }
}