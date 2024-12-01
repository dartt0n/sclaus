package com.dartt0n.sclaus

import zio.*
import zio.Console.*

object Main extends ZIOAppDefault {

  def run: Task[Unit] = for {
    _ <- printLine("Ho, ho ho! Merry Christmas 🎅!")
  } yield ()

}
