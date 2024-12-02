package com.dartt0n.sclaus

import cats.effect.{IO, IOApp}
import cats.effect.ExitCode

object Main extends IOApp {

  def run(args: List[String]) =
    for {
      _ <- IO.println("Ho, ho ho! Merry Chri!stmas 🎅!")
    } yield ExitCode.Success

}
