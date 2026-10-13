package mockit

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class ScalaDeencapsulationTest {
  class Parent {
    private val secret: String = "parent"
    private def hidden(i: Int): Int = i + 3
  }
  class Child extends Parent

  @Test
  def readsInheritedPrivateField(): Unit = {
    val child = new Child
    assertEquals("parent", Deencapsulation.getField[String](child, "secret"))
  }

  @Test
  def invokesPrivateMethodWithDeclaredSignature(): Unit = {
    val child = new Child
    val result: java.lang.Integer = Deencapsulation.invoke[java.lang.Integer](
      child, "hidden", Array[Class[_]](java.lang.Integer.TYPE), Int.box(4))
    assertEquals(7, result.intValue())
  }
}
