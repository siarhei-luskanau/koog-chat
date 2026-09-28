import java.util.Properties

fun isFakeDataEnabled(properties: () -> Properties) =
    (
        System.getProperty("IS_FAKE_DATA_ENABLED")
            ?: properties().getProperty("IS_FAKE_DATA_ENABLED")
    ).toBoolean()
