import sbt._
import Keys._

case class ProjectDefFile(
  user:String,
  projectName:String,
  sourceList:Seq[String], 
  tree:String = "master",
  addHeader:Boolean = true
){

  def sourceURL(sourceName:String) =
    Seq("https://raw.github.com",user,projectName,tree,"project",sourceName).mkString("/")

  def rawSourceList:Seq[(String,String)] = sourceList.map{ l =>
    ( 
      l ->
      {{if(addHeader)
        "package " + user + "." + projectName + ";\n"
      else ""} + 
      scala.io.Source.fromURL(sourceURL(l)).mkString
      }
    )
  }

}

object ProjectDefFiles{
  
  lazy val list = Seq(
    ProjectDefFile("scalaz","scalaz",Seq("Boilerplate.scala","ScalazBuild.scala"))
   ,ProjectDefFile(
      "harrah",
      "xsbt",
      Seq("Sbt.scala","Util.scala","Sbt.scala","Status.scala"
         ,"Sxr.scala","Proguard.scala","Release.scala","Transform.scala"
      ),
      "0.10"
    )
   ,ProjectDefFile(
      "jboner",
      "akka",
      Seq("AkkaBuild.scala","Publish.scala","Unidoc.scala")
      ,addHeader = false
    )
   ,ProjectDefFile(
      "n8han",
      "Unfiltered",
      Seq("build.scala")
    )
   ,ProjectDefFile(
      "mongodb",
      "casbah",
      Seq("CasbahBuild.scala")
    )
  )
}

object ScalaKaigi extends Build{

  lazy val root = Project(
    "root",
    file("."),
    settings = Defaults.defaultSettings ++ Seq(
      resolvers ++= Seq(
        "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
      ) 
     ,libraryDependencies ++= Seq(
        "org.scala-tools.sbt" %% "sbt" % "0.10.1"
       ,"net.databinder" %% "dispatch-http" % "0.8.3"
       ,"com.typesafe.sbt-multi-jvm" %% "sbt-multi-jvm" % "0.1.4"
       ,"com.typesafe.sbt-scalariform" %% "sbt-scalariform" % "0.1.2"
      ),
      addCompilerPlugin("org.scala-tools.sxr" %% "sxr" % "0.2.7")
     ,(sourceGenerators in Compile) <+= (sourceManaged in Compile) map{
        dir =>
        ProjectDefFiles.list.flatMap{ p =>
          p.rawSourceList.map{case (name,str) =>
            IO.createDirectory(dir / p.user / p.projectName ) 
            val f = ( dir / p.user / p.projectName / name ).asFile
            IO.write(f,  str)
            f
          }
        }
      }
    )   

  )

}

