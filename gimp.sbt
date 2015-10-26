val checkGimpTools = taskKey[Unit]("Check the commandline tools xcf2png and xcfinfo")
val compileImages = taskKey[Unit]("Converts Gimp files into png assets")
val testImages = taskKey[Unit]("Check size of images")

checkGimpTools := {
  val missing = Seq("xcf2png", "xcfinfo") filter { name =>
    scala.util.Try {
      Process(name :: "--version" :: Nil).!! == ""
    } getOrElse true
  }

  missing foreach { m =>
    println(s"Command line tool $m is missing")
  }
  assert(missing.isEmpty, "Are required command line tool is missing")
}

compileImages := {
  val sourceFolder = sourceDirectory.value / "main" / "gimp"
  val allFiles = sourceFolder ** "*.xcf"
  val targetFolder = target.value / "gimp" / "images"
  
  val toPngExtension = (f: File) =>
    file(f.getAbsolutePath.replaceAll("xcf$", "png"))

  val mappings = allFiles pair (toPngExtension andThen rebase(sourceFolder, targetFolder))

  println(s"Converting ${mappings.size} gimp files")

  mappings.foreach {case (sourceFile, targetFile) =>
    targetFile.getParentFile.mkdirs()
    Process("xcf2png" :: sourceFile.getAbsolutePath :: "--output" :: targetFile.getAbsolutePath :: Nil).!
    println(targetFile)
  }
}

compileImages <<= compileImages dependsOn checkGimpTools
compile in Compile <<= compile in Compile dependsOn compileImages

testImages := println("check images")

