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
        invocation.proceed[Int](i + 1)
      }
    }
    assertEquals(4, counter.increment(2))
    assertEquals(Vector(2), seenArgs)
  }

  @Test
  def fakesScalaSingleton(): Unit = {
    new MockUp[ScalaSingletonTarget.type] {
      @Mock
      def value(): Int = 7
    }
    assertEquals(7, ScalaSingletonTarget.value())
  }
}

object ScalaSingletonTarget {
  def value(): Int = 1
}
