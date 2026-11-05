cmake_minimum_required(VERSION 3.22.1)
project("gifski_jni" C)
include(FetchContent)

set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS ../../../../rust-toolchain.toml)
unset(Rust_TOOLCHAIN CACHE)
FetchContent_Declare(
        Corrosion
        GIT_REPOSITORY https://github.com/corrosion-rs/corrosion.git
        GIT_TAG v0.6.1
        GIT_SHALLOW 1
)
FetchContent_MakeAvailable(Corrosion)

corrosion_import_crate(
        MANIFEST_PATH gifski/Cargo.toml
        LOCKED
        NO_DEFAULT_FEATURES
        FEATURES gifsicle capi
        CRATE_TYPES staticlib
        FLAGS $<$<NOT:$<CONFIG:Debug>>:-Z build-std=std,panic_abort -Z build-std-features=>
)
corrosion_add_target_rustflags(gifski $<$<NOT:$<CONFIG:Debug>>:-Z unstable-options -C panic=immediate-abort -Z location-detail=none -Z fmt-debug=none -C codegen-units=1>)

add_library(${CMAKE_PROJECT_NAME} SHARED gifski_jni.c)

if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE -O3 -flto)
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE -fvisibility=hidden -fvisibility-inlines-hidden)
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE -ffunction-sections -fdata-sections)

    target_link_options(${CMAKE_PROJECT_NAME} PRIVATE
            -Wl,--gc-sections
            -Wl,--exclude-libs,ALL
            -Wl,--strip-all
    )
    target_link_options(${CMAKE_PROJECT_NAME} PRIVATE -flto)
endif (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")

target_link_libraries(${CMAKE_PROJECT_NAME} android jnigraphics gifski)