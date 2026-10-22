import SwiftUI
import ComposeApp
import GoogleSignIn
import FoundationModels

@main
struct ComposeApp: App {
    init() {
        AppleFoundationModelsBridgeRegistry.shared.bridge = AppleFoundationModelsBridgeImpl()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        if ProcessInfo.processInfo.arguments.contains("-promptSpike") {
            return PromptApiSpikeKt.promptSpikeViewController(bridge: FoundationModelsSpikeBridgeImpl())
        }
        return MainKt.mainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Updates will be handled by Compose
    }
}

final class FoundationModelsSpikeBridgeImpl: NSObject, FoundationModelsSpikeBridge {
    func availability() -> String {
        guard #available(iOS 26.0, *) else { return "unavailable(osBelow26)" }
        switch SystemLanguageModel.default.availability {
        case .available:
            return "available"
        case .unavailable(let reason):
            return "unavailable(\(reason))"
        }
    }

    func stream(prompt: String, listener: FoundationModelsSpikeListener) -> FoundationModelsSpikeCancellable {
        guard #available(iOS 26.0, *) else {
            listener.onComplete(failure: "iOS below 26")
            return SpikeTaskCancellable(task: nil)
        }
        let task = Task {
            var first = true
            do {
                let session = LanguageModelSession()
                for try await snapshot in session.streamResponse(to: prompt) {
                    if first {
                        print("PromptApiSpike: swift first snapshot mainThread=\(Thread.isMainThread)")
                        first = false
                    }
                    listener.onChunk(text: snapshot.content)
                }
                listener.onComplete(failure: nil)
            } catch is CancellationError {
                listener.onComplete(failure: "CancellationError")
            } catch {
                listener.onComplete(failure: "\(type(of: error)): \(error)")
            }
        }
        return SpikeTaskCancellable(task: task)
    }
}

final class AppleFoundationModelsBridgeImpl: NSObject, AppleFoundationModelsBridge {
    func availability() -> String {
        guard #available(iOS 26.0, *) else { return "unavailable(osBelow26)" }
        switch SystemLanguageModel.default.availability {
        case .available:
            return "available"
        case .unavailable(let reason):
            return "unavailable(\(reason))"
        }
    }

    func stream(prompt: String, callback listener: AppleFoundationModelsListener) -> AppleFoundationModelsCancellable {
        guard #available(iOS 26.0, *) else {
            listener.onComplete(failure: "iOS 26 or newer is required")
            return AppleFoundationModelsTaskCancellable(task: nil)
        }
        let task = Task {
            do {
                let session = LanguageModelSession()
                for try await snapshot in session.streamResponse(to: prompt) {
                    listener.onSnapshot(text: snapshot.content)
                }
                listener.onComplete(failure: nil)
            } catch is CancellationError {
                listener.onComplete(failure: "Cancelled")
            } catch {
                listener.onComplete(failure: "\(error)")
            }
        }
        return AppleFoundationModelsTaskCancellable(task: task)
    }
}

final class AppleFoundationModelsTaskCancellable: NSObject, AppleFoundationModelsCancellable {
    private let task: Task<Void, Never>?

    init(task: Task<Void, Never>?) {
        self.task = task
    }

    func cancel() {
        task?.cancel()
    }
}

final class SpikeTaskCancellable: NSObject, FoundationModelsSpikeCancellable {
    private let task: Task<Void, Never>?

    init(task: Task<Void, Never>?) {
        self.task = task
    }

    func cancel() {
        print("PromptApiSpike: swift task cancel")
        task?.cancel()
    }
}
