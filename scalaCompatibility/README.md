# Scala 2.12.18 compatibility checks

This module exercises the MR3-scoped JMockit API on Java 21 without reading MR3 code.

Bootstrap the locally installed agent first, then execute this isolated module:

```sh
./mvnw -pl main -am install -DskipTests -Dlicense.skip=true
./mvnw -pl scalaCompatibility test -Dlicense.skip=true
```

The second command uses the local `2.3.1.mr3.3.0` agent JAR through
Surefire's explicit `-javaagent` argument. The tests must not run with
JMockit 2.3.0. This is NOT a substitute for the real MR3 test suite.

When modifying these tests, preserve assertions and add reproductions of
Scala-specific failures before changing JMockit's faking implementation.
