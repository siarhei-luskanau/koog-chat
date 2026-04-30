package koog.chat.ui.llmconfig

interface LlmConfigNavigationCallback {
    fun openConfigDetails(configId: String?)

    fun goBack()
}
