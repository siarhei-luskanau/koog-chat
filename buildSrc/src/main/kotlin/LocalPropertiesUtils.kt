import java.util.Properties

fun isDataStubEnabled(properties: () -> Properties) =
    (
        System.getProperty("IS_DATA_STUB_ENABLED")
            ?: properties().getProperty("IS_DATA_STUB_ENABLED")
    ).toBoolean()
