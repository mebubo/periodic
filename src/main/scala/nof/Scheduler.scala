package nof

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.MILLISECONDS
import scala.concurrent.duration.TimeUnit
import cats.effect.Clock
import cats.effect.Timer
import cats.effect.Sync
import cats.effect.IO
import cats.implicits._
import cats.instances._

object Scheduler {
    def periodic[A](interval: FiniteDuration, task: IO[A])(implicit t: Timer[IO]): IO[A] = for {
        m <- measure(task)
        (_, elapsed) = m
        remaining = interval - elapsed
        _ <- Timer[IO].sleep(remaining)
        result <- periodic(interval, task)
    } yield result


    def measure[A](fa: IO[A])(implicit c: Clock[IO]): IO[(A, FiniteDuration)] = for {
        start <- Clock[IO].monotonic(MILLISECONDS)
        result <- fa
        finish <- Clock[IO].monotonic(MILLISECONDS)
    } yield (result, FiniteDuration(finish - start, MILLISECONDS))
}
