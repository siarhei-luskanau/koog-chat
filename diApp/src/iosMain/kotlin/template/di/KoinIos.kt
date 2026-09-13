package template.di

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier

@OptIn(BetaInteropApi::class)
internal fun <T> Koin.get(objCClass: ObjCClass): T {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz)
}

@OptIn(BetaInteropApi::class)
internal fun <T> Koin.get(
    objCClass: ObjCClass,
    qualifier: Qualifier?,
    parameter: Any,
): T {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz, qualifier) { parametersOf(parameter) }
}
