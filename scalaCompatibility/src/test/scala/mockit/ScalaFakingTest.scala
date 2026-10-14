package mockit

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class ScalaFakingTest {
  class Counter {
    def increment(i: Int): Int = i + 1
  }

  @Test
  def fakesScalaInstanceMethod(): Unit = {
    val counter = new Counter
    new MockUp[Counter] {
      @Mock
      def increment(i: Int): Int = i + 10
    }
    assertEquals(12, counter.increment(2))
  }

  @Test
  def suppliesInvocationContextAndProceed(): Unit = {
    val counter = new Counter
    var seenArgs = Vector.empty[Int]
    new MockUp[Counter] {
      @Mock
      def increment(invocation: Invocation, i: Int): Int = {
        assertSame(counter, invocation.getInvokedInstance[Counter]())
        assertEquals(1, invocation.getInvocationCount)
        seenArgs = seenArgs :+ invocation.getInvokedArguments()(0).asInstanceOf[Int]
        val replacement: AnyRef = Int.box(i + 1)
        invocation.proceed[java.lang.Integer](replacement).intValue()
      }
    }
    assertEquals(4, counter.increment(2))
    assertEquals(Vector(2), seenArgs)
  }

  @Test
  def fakesScalaSingleton(): Unit = {
    val fake = new MockUp[ScalaSingletonTarget.type] {
      @Mock
      def value(): Int = 7
    }
    assertEquals(7, ScalaSingletonTarget.value())
    assertSame(ScalaSingletonTarget, fake.getMockInstance())
  }

  @Test
  def fakesConcreteScalaTraitMethod(): Unit = {
    val target = new ScalaTraitImplementation
    val unaffected = new ScalaOtherTraitImplementation
    new MockUp[ScalaTraitImplementation] {
      @Mock
      def compute(value: Int): Int = value + 20
    }
    assertEquals(23, target.compute(3))
    assertEquals(4, unaffected.compute(3))
  }

  @Test
  def unmockedInheritedAbstractTraitMethodReturnsDefault(): Unit = {
    val vertex = new MockUp[WorkerVertexSample] {
      @Mock
      def vertexId: Int = 42
      @Mock
      def name: String = "vertex42"
    }.getMockInstance()

    assertEquals(42, vertex.vertexId)
    assertEquals("vertex42", vertex.name)
    assertNull(vertex.seedName)
    assertNull(vertex.dagUgi)
  }

  @Test
  def fakesGenericOverrideViaBridge(): Unit = {
    val target: ScalaGenericBase[String] = new ScalaGenericImplementation
    new MockUp[ScalaGenericImplementation] {
      @Mock
      def transform(value: String): String = "fake-" + value
    }
    assertEquals("fake-hello", target.transform("hello"))
  }
}

trait ScalaTraitDefault {
  def compute(value: Int): Int = value + 1
}
class ScalaTraitImplementation extends ScalaTraitDefault
class ScalaOtherTraitImplementation extends ScalaTraitDefault

class ScalaGenericBase[T] {
  def transform(value: T): T = value
}
class ScalaGenericImplementation extends ScalaGenericBase[String] {
  override def transform(value: String): String = value + "!"
}


sealed trait VertexRuntimeSample {
  def seedName: String
  def dagUgi: String
  def notifyError(cause: Exception): Unit
}
trait WorkerVertexRuntimeSample extends VertexRuntimeSample
trait VertexSample {
  def vertexId: Int
  def name: String
}
trait WorkerVertexSample extends VertexSample with WorkerVertexRuntimeSample

object ScalaSingletonTarget {
  def value(): Int = 1
}
