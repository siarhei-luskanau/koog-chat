// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackageDylib",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackageDylib",
      type: .dynamic,
      targets: ["KotlinMultiplatformLinkedPackageDylib"]
    )
  ],
  dependencies: [
    .package(path: "../io_github_mirzemehdi_kmpauth_google_3_0_6")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackageDylib",
      dependencies: [
        .product(name: "io_github_mirzemehdi_kmpauth_google_3_0_6", package: "io_github_mirzemehdi_kmpauth_google_3_0_6")
      ]
    )
  ]
)
