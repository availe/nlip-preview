package io.availe.testutil

/**
 * Global test flags.
 *
 * * `TEST_ROLLBACK` / `test.rollback` – controls whether every test
 *   transaction is rolled back (default **true**).
 * * `TEST_RESETDB`  / `test.resetdb`  – if **true** the database is
 *   Flyway-cleaned and re-migrated once at JVM start-up.
 */
object TestConfig {
    private fun envOrProp(env: String, prop: String) =
        System.getenv(env)?.lowercase()
            ?: System.getProperty(prop)?.lowercase()

    val rollbackEnabled: Boolean =
        envOrProp("TEST_ROLLBACK", "test.rollback")?.toBooleanStrictOrNull() ?: true

    val resetDb: Boolean =
        envOrProp("TEST_RESETDB", "test.resetdb")?.toBooleanStrictOrNull() ?: false
}
