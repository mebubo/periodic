package nof

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.MILLISECONDS
import scala.concurrent.duration.SECONDS
import scala.concurrent.duration.TimeUnit
import cats.effect.Clock
import cats.effect.Timer
import cats.effect.IO
import cats.implicits._
import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.MonadError

object App {
    val duration = FiniteDuration(1, SECONDS)

    def print(s: String): IO[Unit] = IO(println(s))
    def read: IO[String] = IO(readLine)

    def doingAndCounting[A](f: Int => IO[A])(implicit T: Timer[IO]): IO[Unit] = for {
        ref <- Ref.of[IO, Int](0)
        p = for {
            i <- ref.get
            _ <- f(i)
            _ <- ref.modify(x => (x + 1, x))
        } yield ()
        _ <- Scheduler.periodic(duration, p)
    } yield ()

    def doing[A](fa: IO[A])(implicit T: Timer[IO]): IO[Unit] = Scheduler.periodic(duration, fa).map(_ => ())

    def app(implicit C: Concurrent[IO], T: Timer[IO]): IO[Unit] = {
        val printOrError: Int => IO[Unit] = i => if (i == 5) IO.raiseError(new Exception("fail")) else print(s"hello $i")
        for {
            token1 <- Concurrent[IO].start(doing(print("hello")))
            token2 <- Concurrent[IO].start(doingAndCounting(printOrError))
            _ <- read
            _ <- token1.cancel
            _ <- token2.cancel
            _ <- print("stop")
        } yield ()
    }
}