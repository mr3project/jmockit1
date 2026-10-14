package mockit

import org.junit.{After, Before, Test}
import org.junit.Assert._

import scala.collection.mutable.ArrayBuffer

/** Mirrors the MR3 JUnit 4 @Before fake-installation / @After validation lifecycle. */
class ScalaJunit4LifecycleTest {
  private val allDags = ArrayBuffer.empty[Int]

  @Before
  def before(): Unit = {
    allDags.synchronized {
      allDags.clear()

      new MockUp[ScalaDagFactorySample.type] {
        @Mock
        def apply(
            inv: Invocation,
            dagId: Int,
            dagName: String,
            config: ScalaDagConfigSample,
            userName: String,
            submitter: String,
            resources: Map[String, String],
            metaData: Map[String, Long],
            priority: Int,
            queue: String,
            appContext: ScalaDagContextSample,
            jobConf: Option[String]): ScalaDagFactorySample = {
          allDags.synchronized {
            assertFalse("duplicate DAG", allDags.contains(dagId))
            allDags += dagId
          }
          inv.proceed[ScalaDagFactorySample]()
        }
      }

      new MockUp[ScalaDagInstanceSample] {
        @Mock
        def terminate(inv: Invocation, isFatal: Boolean): Unit = {
          val dagId = inv.getInvokedInstance[ScalaDagFactorySample]().dagId
          allDags.synchronized {
            assertTrue("DAG.apply was not monitored", allDags.contains(dagId))
            allDags -= dagId
          }
          inv.proceed[Unit]()
        }
      }
    }
  }

  @After
  def after(): Unit = {
    assertTrue("DAG was not terminated: " + allDags, allDags.isEmpty)
  }

  @Test
  def firstDag(): Unit = runDAG(15)

  @Test
  def secondDag(): Unit = runDAG(16)

  private def runDAG(dagId: Int): Unit = {
    val dag = ScalaDagFactorySample(
      dagId, "dag", new ScalaDagConfigSample, "user", "submitter",
      Map("file" -> "resource"), Map("len" -> 4L), 10, "queue",
      new ScalaDagContextSample, Some("conf"))
    assertTrue("DAG.apply should append DAG ID", allDags.contains(dagId))
    dag.terminate(false)
  }
}
