package com.jetbrains.kmpapp.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("com.jetbrains.kmpapp.other.android")
actual class NativeModuleC