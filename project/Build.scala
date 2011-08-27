import sbt._
import Keys._

case class ProjectDefFile(
  user:String,
  projectName:String,
  sourceList:Seq[String], 
  tree:String = "master"
){

  def sourceURL(sourceName:String) =
    Seq("https://raw.github.com",user,projectName,tree,sourceName).mkString("/")

  lazy val rawSourceList:Seq[(String,String)] = sourceList.map{ s =>
    s -> scala.io.Source.fromURL(sourceURL(s)).mkString
  }

}

object ProjectDefFiles{
  
  lazy val list = Seq(
    ProjectDefFile("scalaz","scalaz",Seq("project/Boilerplate.scala","project/ScalazBuild.scala"))
    //ProjectDefFile("","",""),

  )
}

object ScalaKaigi extends Build{

  lazy val root = Project(
    "root",
    file("."),
    settings = Defaults.defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.scala-tools.sbt" %% "sbt" % "0.10.0"
      ),
      addCompilerPlugin("org.scala-tools.sxr" %% "sxr" % "0.2.7")
     ,(sourceGenerators in Compile) <+= (sourceManaged in Compile) map{
        dir =>
        ProjectDefFiles.list.flatMap{ p =>
          p.rawSourceList.map{case (name,str) =>
            IO.createDirectory(dir / p.user / p.projectName ) 
            val f = ( dir / p.user / p.projectName / name ).asFile
            IO.write(f,str)
            f
          }
        }
      }
    )   

  )

}

