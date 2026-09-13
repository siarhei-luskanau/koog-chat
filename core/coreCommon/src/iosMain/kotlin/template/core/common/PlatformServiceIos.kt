package template.core.common

import org.koin.core.annotation.Single

@Single
internal class PlatformServiceIos : PlatformService {
    override fun setStrictMode(isEnabled: Boolean) = Unit
}
