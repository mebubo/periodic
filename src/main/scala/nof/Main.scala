package nof

import cats.effect.IO
import cats.effect.Timer
import cats.effect.IOApp
import cats.effect.ExitCode

object Main extends IOApp {
    def run(args: List[String]): IO[ExitCode] = {
        App.app.map(_ => ExitCode.Success)
    }
}