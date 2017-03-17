package io.vandam.dbdeploy;

/**
 * The Enum Activity.
 */
enum Activity {

    /**
     * Show help.
     */
    HELP,

    /**
     * Initialise configuration.
     */
    INITIALISE,

    /**
     * Import static data
     */
    IMPORT_DATA,

    /**
     * Import source.
     */
    IMPORT,

    /**
     * Compare against target.
     */
    COMPARE,

    /**
     * CompareAndApply differences to target.
     */
    APPLY,

    /**
     * Test activity.
     */
    TEST,

    /**
     * Import static data from tab delimited file
     */
    DATA_FROM_TXT,

    /**
     * Create test configuration sample
     */
    CREATE_TEST_SAMPLE
}
