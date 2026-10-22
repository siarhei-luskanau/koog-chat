@file:OptIn(ExperimentalWasmJsInterop::class)

package koog.chat.core.llm.ondevice

import kotlin.js.ExperimentalWasmJsInterop

internal fun installFakeLanguageModel(): Unit =
    js(
        """(function () {
        if (!globalThis.__fakeLmSaved) {
            globalThis.__fakeLmSaved = {
                had: Object.prototype.hasOwnProperty.call(globalThis, 'LanguageModel'),
                value: globalThis.LanguageModel
            };
        }
        var fake = {
            availabilityValue: 'available',
            availabilityError: null,
            availabilityCalls: 0,
            availabilityInputs: null,
            createMode: 'resolve',
            createCalls: 0,
            createInputs: null,
            createInitialPrompts: null,
            createSignal: null,
            createHadMonitor: false,
            createdDuringGesture: false,
            requireGesture: false,
            userActivation: false,
            pendingCreate: null,
            monitorTarget: null,
            contextWindow: 9216,
            destroyCount: 0,
            streamMode: 'close',
            streamError: 'stream failed',
            chunks: [],
            promptCalls: 0,
            promptText: null,
            promptSignal: null,
            readerCancelCount: 0
        };
        function makeSession() {
            return {
                contextWindow: fake.contextWindow,
                destroy: function () { fake.destroyCount++; },
                promptStreaming: function (text, opts) {
                    fake.promptCalls++;
                    fake.promptText = text;
                    fake.promptSignal = opts ? opts.signal : null;
                    var index = 0;
                    return new ReadableStream({
                        pull: function (controller) {
                            if (index < fake.chunks.length) {
                                controller.enqueue(fake.chunks[index++]);
                                return undefined;
                            }
                            if (fake.streamMode === 'error') {
                                controller.error(new Error(fake.streamError));
                                return undefined;
                            }
                            if (fake.streamMode === 'hang') return new Promise(function () {});
                            controller.close();
                            return undefined;
                        },
                        cancel: function () { fake.readerCancelCount++; }
                    }, { highWaterMark: 0 });
                }
            };
        }
        globalThis.__fakeLm = fake;
        globalThis.LanguageModel = {
            availability: function (options) {
                fake.availabilityCalls++;
                fake.availabilityInputs = JSON.stringify([options.expectedInputs, options.expectedOutputs]);
                if (fake.availabilityError !== null) return Promise.reject(new Error(fake.availabilityError));
                return Promise.resolve(fake.availabilityValue);
            },
            create: function (options) {
                fake.createCalls++;
                fake.createInputs = JSON.stringify([options.expectedInputs, options.expectedOutputs]);
                fake.createInitialPrompts = options.initialPrompts ? JSON.stringify(options.initialPrompts) : null;
                fake.createSignal = options.signal || null;
                fake.createdDuringGesture = fake.userActivation;
                fake.createHadMonitor = typeof options.monitor === 'function';
                if (fake.createHadMonitor) {
                    fake.monitorTarget = new EventTarget();
                    options.monitor(fake.monitorTarget);
                }
                if (fake.requireGesture && !fake.userActivation) {
                    return Promise.reject(new DOMException('Requires a user gesture', 'NotAllowedError'));
                }
                if (fake.createMode === 'reject') return Promise.reject(new Error('create failed'));
                if (fake.createMode === 'pending') {
                    return new Promise(function (resolve, reject) {
                        fake.pendingCreate = { resolve: resolve, reject: reject };
                    });
                }
                return Promise.resolve(makeSession());
            }
        };
        fake.makeSession = makeSession;
    })()""",
    )

internal fun uninstallFakeLanguageModel(): Unit =
    js(
        """(function () {
        var saved = globalThis.__fakeLmSaved;
        if (saved) {
            if (saved.had) globalThis.LanguageModel = saved.value; else delete globalThis.LanguageModel;
        }
        delete globalThis.__fakeLmSaved;
        delete globalThis.__fakeLm;
    })()""",
    )

internal fun removeLanguageModel(): Unit = js("(function () { delete globalThis.LanguageModel; })()")

internal fun fakeSetAvailability(value: String): Unit = js("(function () { globalThis.__fakeLm.availabilityValue = value; })()")

internal fun fakeFailAvailability(message: String): Unit = js("(function () { globalThis.__fakeLm.availabilityError = message; })()")

internal fun fakeAvailabilityCalls(): Int = js("globalThis.__fakeLm.availabilityCalls")

internal fun fakeAvailabilityInputs(): String? = js("globalThis.__fakeLm.availabilityInputs")

internal fun fakeSetCreateMode(mode: String): Unit = js("(function () { globalThis.__fakeLm.createMode = mode; })()")

internal fun fakeCreateCalls(): Int = js("globalThis.__fakeLm.createCalls")

internal fun fakeCreateInputs(): String? = js("globalThis.__fakeLm.createInputs")

internal fun fakeCreateInitialPrompts(): String? = js("globalThis.__fakeLm.createInitialPrompts")

internal fun fakeCreateHadMonitor(): Boolean = js("globalThis.__fakeLm.createHadMonitor")

internal fun fakeCreateSignalAborted(): Boolean =
    js("globalThis.__fakeLm.createSignal !== null && globalThis.__fakeLm.createSignal.aborted")

internal fun fakeCreatedDuringGesture(): Boolean = js("globalThis.__fakeLm.createdDuringGesture")

internal fun fakeRequireGesture(): Unit = js("(function () { globalThis.__fakeLm.requireGesture = true; })()")

internal fun fakeSetUserActivation(active: Boolean): Unit = js("(function () { globalThis.__fakeLm.userActivation = active; })()")

internal fun fakeResolvePendingCreate(): Unit =
    js("(function () { var f = globalThis.__fakeLm; f.pendingCreate.resolve(f.makeSession()); })()")

internal fun fakeRejectPendingCreate(message: String): Unit =
    js("(function () { globalThis.__fakeLm.pendingCreate.reject(new Error(message)); })()")

internal fun fakeEmitDownloadProgress(loaded: Double): Unit =
    js(
        """(function () {
        globalThis.__fakeLm.monitorTarget.dispatchEvent(Object.assign(new Event('downloadprogress'), { loaded: loaded }));
    })()""",
    )

internal fun fakeSetContextWindow(tokens: Double): Unit = js("(function () { globalThis.__fakeLm.contextWindow = tokens; })()")

internal fun fakeDestroyCount(): Int = js("globalThis.__fakeLm.destroyCount")

internal fun fakeAddChunk(text: String): Unit = js("(function () { globalThis.__fakeLm.chunks.push(text); })()")

internal fun fakeSetStreamMode(mode: String): Unit = js("(function () { globalThis.__fakeLm.streamMode = mode; })()")

internal fun fakeSetStreamError(message: String): Unit = js("(function () { globalThis.__fakeLm.streamError = message; })()")

internal fun fakePromptCalls(): Int = js("globalThis.__fakeLm.promptCalls")

internal fun fakePromptText(): String? = js("globalThis.__fakeLm.promptText")

internal fun fakePromptSignalAborted(): Boolean =
    js("globalThis.__fakeLm.promptSignal !== null && globalThis.__fakeLm.promptSignal.aborted")

internal fun fakeReaderCancelCount(): Int = js("globalThis.__fakeLm.readerCancelCount")
